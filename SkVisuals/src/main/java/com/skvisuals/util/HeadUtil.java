package com.skvisuals.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile as BukkitPlayerProfile;

import java.util.Base64;
import java.util.Optional;

public final class HeadUtil {

    public static ItemStack createHead(String base64Texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            PlayerProfile profile = Bukkit.createProfile(Bukkit.getOfflinePlayer("SkVisualsHead").getUniqueId(), "SkVisualsHead");
            profile.setProperty(new ProfileProperty("textures", base64Texture, null));
            meta.setPlayerProfile(profile);
            head.setItemMeta(meta);
        }
        return head;
    }

    public static void applyTexture(ItemStack item, String base64Texture) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof SkullMeta skullMeta) {
            PlayerProfile profile = Bukkit.createProfile(Bukkit.getOfflinePlayer("SkVisualsHead").getUniqueId(), "SkVisualsHead");
            profile.setProperty(new ProfileProperty("textures", base64Texture, null));
            skullMeta.setPlayerProfile(profile);
            item.setItemMeta(skullMeta);
        }
    }

    public static void applyTexture(Block block, String base64Texture) {
        BlockState state = block.getState();
        if (state instanceof Skull skull) {
            PlayerProfile profile = Bukkit.createProfile(Bukkit.getOfflinePlayer("SkVisualsHead").getUniqueId(), "SkVisualsHead");
            profile.setProperty(new ProfileProperty("textures", base64Texture, null));
            skull.setPlayerProfile(profile);
            skull.update(true);
        }
    }

    public static Optional<String> extractBase64(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof SkullMeta skullMeta) {
            BukkitPlayerProfile profile = skullMeta.getPlayerProfile();
            if (profile != null) {
                ProfileProperty prop = profile.getProperty("textures");
                if (prop != null) return Optional.of(prop.getValue());
            }
        }
        return Optional.empty();
    }

    public static Optional<String> extractBase64(Block block) {
        BlockState state = block.getState();
        if (state instanceof Skull skull) {
            BukkitPlayerProfile profile = skull.getPlayerProfile();
            if (profile != null) {
                ProfileProperty prop = profile.getProperty("textures");
                if (prop != null) return Optional.of(prop.getValue());
            }
        }
        return Optional.empty();
    }

    public static Optional<String> extractBase64(Player player) {
        BukkitPlayerProfile profile = player.getPlayerProfile();
        if (profile != null) {
            ProfileProperty prop = profile.getProperty("textures");
            if (prop != null) return Optional.of(prop.getValue());
        }
        return Optional.empty();
    }

    public static Optional<String> extractBase64(org.bukkit.OfflinePlayer offline) {
        BukkitPlayerProfile profile = offline.getPlayerProfile();
        if (profile != null) {
            ProfileProperty prop = profile.getProperty("textures");
            if (prop != null) return Optional.of(prop.getValue());
        }
        return Optional.empty();
    }

    public static Optional<String> decodeTextureUrl(String base64) {
        try {
            String json = new String(Base64.getDecoder().decode(base64));
            // Simple extraction of URL from JSON
            int urlIndex = json.indexOf("\"url\":\"");
            if (urlIndex >= 0) {
                int start = urlIndex + 7;
                int end = json.indexOf("\"", start);
                return Optional.of(json.substring(start, end));
            }
        } catch (Exception ignored) {}
        return Optional.empty();
    }
}