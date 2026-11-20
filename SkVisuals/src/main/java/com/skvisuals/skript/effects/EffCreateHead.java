package com.skvisuals.skript.effects;

import com.skvisuals.util.HeadUtil;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import ch.njol.skript.aliases.ItemType;

public class EffCreateHead extends Effect {
    private Expression<String> textureString;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        textureString = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    protected void execute(Event event) {
        String tex = textureString.getSingle(event);
        if (tex != null) {
            HeadUtil.createHead(tex);
        }
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "create custom head with texture " + textureString.toString(event, debug);
    }
}
