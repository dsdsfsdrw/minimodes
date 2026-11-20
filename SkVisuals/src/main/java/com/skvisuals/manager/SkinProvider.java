package com.skvisuals.manager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skvisuals.SkVisuals;
import com.skvisuals.util.SkinUtil;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Asynchronous skin texture provider with fallback chain and in-memory caching.
 * Contacts Mojang API first, then configured fallback APIs on failure or HTTP 429.
 */
public final class SkinProvider {
    private final SkVisuals plugin;
    private final HttpClient httpClient;
    private final CacheManager cache;
    private final List<String> fallbackApis;
    private final boolean debug;

    private final ConcurrentHashMap<String, CompletableFuture<String>> pending = new ConcurrentHashMap<>();

    public SkinProvider(SkVisuals plugin, CacheManager cache, List<String> fallbackApis, boolean debug) {
        this.plugin = plugin;
        this.cache = cache;
        this.fallbackApis = fallbackApis;
        this.debug = debug;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public CompletableFuture<String> resolveTextureBase64(String username) {
        String cacheKey = "texture:" + username.toLowerCase();
        String cached = cache.get(cacheKey);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return pending.computeIfAbsent(cacheKey, key -> fetchWithFallbacks(username)
                .thenApply(result -> {
                    cache.put(cacheKey, result);
                    return result;
                })
                .whenComplete((r, t) -> pending.remove(key)));
    }

    private CompletableFuture<String> fetchWithFallbacks(String username) {
        return fetchMojang(username).handle((result, ex) -> {
            if (result != null) return result;
            if (ex != null && isRateLimited(ex)) {
                log("Mojang rate-limited for " + username + ", trying fallbacks.");
            } else if (ex != null) {
                log("Mojang failed for " + username + ": " + ex.getMessage() + ", trying fallbacks.");
            }
            return null;
        }).thenCompose(res -> {
            if (res != null) return CompletableFuture.completedFuture(res);
            return tryFallbacks(username);
        });
    }

    private CompletableFuture<String> tryFallbacks(String username) {
        CompletableFuture<String> result = CompletableFuture.failedFuture(new IOException("no fallback apis configured"));
        for (String template : fallbackApis) {
            String url = template.replace("%name%", username);
            CompletableFuture<String> current = fetchJsonTexture(url);
            result = result.handle((r, ex) -> {
                if (r != null) return r;
                return null;
            }).thenCompose(ignored -> current);
        }
        return result;
    }

    private CompletableFuture<String> fetchMojang(String username) {
        String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + username + "?unsigned=false";
        return fetchJsonTexture(url);
    }

    private CompletableFuture<String> fetchJsonTexture(String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(java.time.Duration.ofSeconds(8))
                        .header("Accept", "application/json")
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                int code = response.statusCode();
                String body = response.body();

                if (code == 429) {
                    throw new IOException("HTTP 429 Too Many Requests");
                }
                if (code < 200 || code >= 300) {
                    throw new IOException("HTTP " + code + " for " + url);
                }

                JsonObject root = JsonParser.parseString(body).getAsJsonObject();
                String base64 = extractBase64(root);
                if (base64 == null) {
                    throw new IOException("No textures property in response from " + url);
                }
                return base64;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).exceptionally(ex -> {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            log("Failed to fetch texture from " + url + ": " + cause.getMessage());
            return null;
        });
    }

    private String extractBase64(JsonObject root) {
        if (root.has("properties")) {
            var properties = root.getAsJsonArray("properties");
            for (var prop : properties) {
                JsonObject p = prop.getAsJsonObject();
                if ("textures".equals(p.get("name").getAsString())) {
                    return p.get("value").getAsString();
                }
            }
        }
        return null;
    }

    public void applySkin(Player target, String base64, String signature, List<Player> forViewers) {
        PlayerProfile profile = Bukkit.createProfile(target.getUniqueId(), target.getName());
        profile.setProperty(new ProfileProperty("textures", base64, signature));
        target.setPlayerProfile(profile);

        if (forViewers != null && !forViewers.isEmpty()) {
            for (Player viewer : forViewers) {
                viewer.hidePlayer(plugin, target);
                viewer.showPlayer(plugin, target);
            }
        } else {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.equals(target)) continue;
                online.hidePlayer(plugin, target);
                online.showPlayer(plugin, target);
            }
        }
    }

    public void resetSkin(Player target, String defaultBase64, List<Player> forViewers) {
        applySkin(target, defaultBase64, null, forViewers);
    }

    public CompletableFuture<String> resolveUrl(String textureUrlOrHash) {
        return SkinUtil.resolveToBase64(textureUrlOrHash, httpClient, cache, debug, log -> plugin.getLogger().info(log));
    }

    public void shutdown() {
        cache.shutdown();
        pending.clear();
    }

    private boolean isRateLimited(Throwable ex) {
        return ex.getMessage() != null && ex.getMessage().contains("429");
    }

    private void log(String msg) {
        if (debug) plugin.getLogger().info("[SkinProvider] " + msg);
    }
}
