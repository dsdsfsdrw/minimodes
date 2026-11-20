package com.skvisuals.skript.effects;

import com.skvisuals.SkVisuals;
import com.skvisuals.manager.SkinProvider;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EffSetSkin extends Effect {
    private Expression<Player[]> targets;
    private Expression<String> value;
    private Expression<String> kind;
    private Expression<String> signature;
    private Expression<Player[]> viewers;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        targets = (Expression<Player[]>) exprs[0];
        kind = (Expression<String>) exprs[1];
        value = (Expression<String>) exprs[2];
        signature = matchedPattern > 1 ? (Expression<String>) exprs[3] : null;
        viewers = matchedPattern > 2 ? (Expression<Player[]>) exprs[3] : null;
        return true;
    }

    @Override
    protected void execute(Event event) {
        Player[] ts = targets.getArray(event);
        Player[] vs = viewers != null ? viewers.getArray(event) : null;
        if (ts == null || value == null) return;

        String base64 = value.getSingle(event);
        String sig = signature != null ? signature.getSingle(event) : null;
        String mode = kind != null ? kind.getSingle(event) : null;

        for (Player p : ts) {
            if (p == null) continue;
            CompletableFuture.runAsync(() -> {
                SkVisuals plugin = SkVisuals.getInstance();
                if (plugin == null || plugin.getSkinProvider() == null) return;

                String resolved;
                if ("base64".equalsIgnoreCase(mode)) {
                    resolved = base64;
                } else if ("url".equalsIgnoreCase(mode) || "player".equalsIgnoreCase(mode)) {
                    try {
                        resolved = plugin.getSkinProvider().resolveUrl(base64).join();
                    } catch (Exception e) {
                        return;
                    }
                } else {
                    try {
                        resolved = plugin.getSkinProvider().resolveTextureBase64(base64).join();
                    } catch (Exception e) {
                        return;
                    }
                }

                plugin.getSkinProvider().applySkin(p, resolved, sig, vs == null ? null : java.util.Arrays.asList(vs));
            });
        }
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "set skin to " + value.toString(event, debug);
    }
}
