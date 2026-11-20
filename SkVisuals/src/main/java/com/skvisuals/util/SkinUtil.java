package com.skvisuals.util;

import com.skvisuals.manager.CacheManager;
import java.net.http.HttpClient;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class SkinUtil {

    public static CompletableFuture<String> resolveToBase64(String input, HttpClient client, CacheManager cache, boolean debug, Consumer<String> logger) {
        if (input.startsWith("eyJ")) { // Likely Base64
            return CompletableFuture.completedFuture(input);
        }
        
        String url = input.startsWith("http") ? input : "http://textures.minecraft.net/texture/" + input;
        String cacheKey = "url:" + url;
        String cached = cache.get(cacheKey);
        if (cached != null) return CompletableFuture.completedFuture(cached);

        String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}";
        String base64 = Base64.getEncoder().encodeToString(json.getBytes());
        cache.put(cacheKey, base64);
        return CompletableFuture.completedFuture(base64);
    }
}
