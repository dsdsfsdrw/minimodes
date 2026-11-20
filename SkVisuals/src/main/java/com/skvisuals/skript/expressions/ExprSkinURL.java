package com.skvisuals.skript.expressions;

import com.skvisuals.util.HeadUtil;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.Optional;

public class ExprSkinURL extends SimpleExpression<String> {
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
        Optional<String> base64 = HeadUtil.extractBase64(p);
        if (base64.isEmpty()) return new String[0];
        Optional<String> url = HeadUtil.decodeTextureUrl(base64.get());
        return url.map(s -> new String[]{s}).orElse(new String[0]);
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "skin url of " + player.toString(event, debug);
    }
}