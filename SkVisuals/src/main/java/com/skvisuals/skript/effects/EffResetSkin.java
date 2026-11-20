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

public class EffResetSkin extends Effect {
    private Expression<Player[]> targets;
    private Expression<Player[]> viewers;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        targets = (Expression<Player[]>) exprs[0];
        viewers = matchedPattern > 0 ? (Expression<Player[]>) exprs[1] : null;
        return true;
    }

    @Override
    protected void execute(Event event) {
        Player[] ts = targets.getArray(event);
        Player[] vs = viewers != null ? viewers.getArray(event) : null;
        if (ts == null) return;

        SkVisuals plugin = SkVisuals.getInstance();
        if (plugin == null || plugin.getSkinProvider() == null) return;

        String defaultBase64 = plugin.getConfig().getString("default-skin-base64", "");
        for (Player p : ts) {
            if (p == null) continue;
            plugin.getSkinProvider().resetSkin(p, defaultBase64,
                    vs == null ? null : java.util.Arrays.asList(vs));
        }
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "reset skin of " + targets.toString(event, debug);
    }
}
