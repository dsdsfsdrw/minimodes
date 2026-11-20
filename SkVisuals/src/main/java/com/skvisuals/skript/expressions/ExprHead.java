package com.skvisuals.skript.expressions;

import com.skvisuals.util.HeadUtil;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;

public class ExprHead extends SimpleExpression<ItemType> {
    private Expression<String> texture;

    @Override
    public Class<? extends ItemType> getReturnType() {
        return ItemType.class;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        texture = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    protected ItemType[] get(Event event) {
        String tex = texture.getSingle(event);
        if (tex == null) return new ItemType[0];
        return new ItemType[]{new ItemType(HeadUtil.createHead(tex))};
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "custom head with texture " + texture.toString(event, debug);
    }
}
