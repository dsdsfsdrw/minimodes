package com.skvisuals.skript.conditions;

import com.skvisuals.util.HeadUtil;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class CondHasCustomSkin extends Condition {
    private Expression<Player> player;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        player = (Expression<Player>) exprs[0];
        return true;
    }

    @Override
    public boolean check(Event event) {
        Player p = player.getSingle(event);
        if (p == null) return false;
        return HeadUtil.extractBase64(p).isPresent();
    }

    @Override
    public String toString(Event event, boolean debug) {
        return player.toString(event, debug) + " has custom skin";
    }
}