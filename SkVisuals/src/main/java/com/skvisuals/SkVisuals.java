package com.skvisuals;

import com.skvisuals.skript.effects.EffCreateHead;
import com.skvisuals.skript.effects.EffDrawLine;
import com.skvisuals.skript.effects.EffDrawShape;
import com.skvisuals.skript.effects.EffSetSkin;
import com.skvisuals.skript.effects.EffSetTexture;
import com.skvisuals.skript.effects.EffSpawnParticle;
import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class SkVisuals extends JavaPlugin {
    private static SkVisuals instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        if (getServer().getPluginManager().getPlugin("Skript") != null) {
            registerSkriptElements();
            getLogger().info("SkVisuals enabled with Skript support.");
        } else {
            getLogger().severe("Skript not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void registerSkriptElements() {
        Skript.registerEffect(EffCreateHead.class, "[(custom|base64)] (head|skull) (with|from) [texture] %string%");
        Skript.registerEffect(EffSetTexture.class, "set [base64] texture of %itemtype/block% to %string%");
        Skript.registerEffect(EffSetSkin.class, "set skin of %players% to [player] %string% [for %-players%]");
        Skript.registerEffect(EffSpawnParticle.class, "spawn %integer% of [particle] %string% at %location% [...]");
        Skript.registerEffect(EffDrawShape.class, "draw [particle] (circle|ring|sphere|filled sphere|helix|spiral|cube|star) of %string% at %location% [...]");
        Skript.registerEffect(EffDrawLine.class, "draw [particle] (line|beam|wave) of %string% from %location% to %location% [...]");
    }

    public static SkVisuals getInstance() { return instance; }
}
