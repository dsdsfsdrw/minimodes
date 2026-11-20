package com.skvisuals.skript.effects;

import com.skvisuals.SkVisuals;
import com.skvisuals.manager.SkinProvider;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EffCopySkin extends Effect {
    private Expression<Player> source;
    private Expression<Player> targets;
    private Expression<Player> viewers;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        source = (Expression<Player>) exprs[0];
        targets = (Expression<Player>) exprs[1];
        viewers = matchedPattern > 1 ? (Expression<Player>) exprs[2] : null;
        return true;
    }

    @Override
    protected void execute(Event event) {
        Player src = source.getSingle(event);
        Player[] ts = targets.getArray(event);
        Player[] vs = viewers != null ? viewers.getArray(event) : null;
        if (src == null || ts == null) return;

        CompletableFuture.runAsync(() -> {
            SkVisuals plugin = SkVisuals.getInstance();
            if (plugin == null || plugin.getSkinProvider() == null) return;

            try {
                String base64 = plugin.getSkinProvider().resolveTextureBase64(src.getName()).join();
                for (Player p : ts) {
                    if (p == null) continue;
                    plugin.getSkinProvider().applySkin(p, base64, null,
                            vs == null ? null : java.util.Arrays.asList(vs));
                }
            } catch (Exception ignored) {}
        });
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "copy skin of " + source.toString(event, debug) + " to " + targets.toString(event, debug);
    }
}
