package com.skvisuals.skript.expressions;

import com.skvisuals.util.HeadUtil;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.Optional;

public class ExprSkinBase64 extends SimpleExpression<String> {
    private Expression<Player> player;

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        player = (Expression<Player>) exprs[0];
        return true;
    }

    @Override
    protected String[] get(Event event) {
        Player p = player.getSingle(event);
        if (p == null) return new String[0];
        Optional<String> b = HeadUtil.extractBase64(p);
        return b.map(s -> new String[]{s}).orElse(new String[0]);
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "skin base64 of " + player.toString(event, debug);
    }
}
