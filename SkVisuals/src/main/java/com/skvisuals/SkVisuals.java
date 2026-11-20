package com.skvisuals;

import com.skvisuals.manager.CacheManager;
import com.skvisuals.manager.SkinProvider;
import com.skvisuals.skript.effects.EffCopySkin;
import com.skvisuals.skript.effects.EffCreateHead;
import com.skvisuals.skript.effects.EffDrawLine;
import com.skvisuals.skript.effects.EffDrawShape;
import com.skvisuals.skript.effects.EffResetSkin;
import com.skvisuals.skript.effects.EffSetSkin;
import com.skvisuals.skript.effects.EffSetTexture;
import com.skvisuals.skript.effects.EffSpawnParticle;
import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SkVisuals extends JavaPlugin implements CommandExecutor, TabCompleter {
    private static SkVisuals instance;
    private CacheManager cacheManager;
    private SkinProvider skinProvider;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadConfig();

        long ttl = getConfig().getLong("cache-ttl-minutes", 60);
        boolean debug = getConfig().getBoolean("debug-mode", false);
        List<String> fallbackApis = getConfig().getStringList("fallback-apis");

        cacheManager = new CacheManager(ttl);
        skinProvider = new SkinProvider(this, cacheManager, fallbackApis, debug);

        if (getServer().getPluginManager().getPlugin("Skript") != null) {
            registerSkriptElements();
            getLogger().info("SkVisuals enabled with Skript support.");
        } else {
            getLogger().severe("Skript not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
        }

        getCommand("skvisuals").setExecutor(this);
        getCommand("skvisuals").setTabCompleter(this);
    }

    @Override
    public void onDisable() {
        if (cacheManager != null) {
            cacheManager.shutdown();
        }
    }

    private void registerSkriptElements() {
        Skript.registerEffect(EffCreateHead.class, "[(custom|base64)] (head|skull) (with|from) [texture] %string%");
        Skript.registerEffect(EffSetTexture.class, "set [base64] texture of %itemtype/block% to %string%");
        Skript.registerEffect(EffSetSkin.class, "set skin of %players% to [player] %string% [for %-players%]");
        Skript.registerEffect(EffCopySkin.class, "copy skin of %player% to %players% [for %-players%]");
        Skript.registerEffect(EffResetSkin.class, "reset skin of %players% [for %-players%]");
        Skript.registerEffect(EffSpawnParticle.class, "spawn %integer% of [particle] %string% at %location% [with offset %number%, %number%, %number%] [speed %number%] [color %color%] [size %number%] [(for %-players%|in radius %-number%)]");
        Skript.registerEffect(EffDrawShape.class, "draw [particle] (circle|ring|sphere|filled sphere|helix|spiral|cube|star) of %string% at %location% with radius %number% [points %integer%] [height %number%] [rotated by %number%, %number%, %number%] [color %color%] [size %number%] [(for %-players%|in radius %-number%)]");
        Skript.registerEffect(EffDrawLine.class, "draw [particle] (line|beam|wave) of %string% from %location% to %location% [step %number%] [amplitude %number%] [frequency %number%] [color %color%] [size %number%] [(for %-players%|in radius %-number%)]");

        Skript.registerExpression(ExprTextureBase64.class, String.class, 0, "[base64] texture of %player/offlineplayer/itemtype/block%");
        Skript.registerExpression(ExprTextureURL.class, String.class, 0, "texture url of %player/offlineplayer/itemtype/block%");
        Skript.registerExpression(ExprSkinBase64.class, String.class, 0, "skin (base64|texture) of %player%");
        Skript.registerExpression(ExprSkinSignature.class, String.class, 0, "skin signature of %player%");
        Skript.registerExpression(ExprSkinURL.class, String.class, 0, "skin url of %player%");
        Skript.registerCondition(CondHasCustomSkin.class, "%player% has [a] custom skin");
    }

    public static SkVisuals getInstance() { return instance; }
    public CacheManager getCacheManager() { return cacheManager; }
    public SkinProvider getSkinProvider() { return skinProvider; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("skvisuals.admin")) {
            sender.sendMessage("§cУ вас нет прав на эту команду.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§eSkVisuals §7— §7используйте /skvisuals reload или /skvisuals clearcache");
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload":
                reloadConfig();
                sender.sendMessage("§aКонфигурация SkVisuals перезагружена.");
                break;
            case "clearcache":
                if (cacheManager != null) cacheManager.clear();
                sender.sendMessage("§aКэш SkVisuals очищен.");
                break;
            default:
                sender.sendMessage("§eДоступные команды: §a/skvisuals reload §7или §a/skvisuals clearcache");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("reload");
            completions.add("clearcache");
        }
        return completions;
    }
}
