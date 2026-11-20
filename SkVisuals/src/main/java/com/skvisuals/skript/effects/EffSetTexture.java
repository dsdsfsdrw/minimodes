package com.skvisuals.skript.effects;

import com.skvisuals.util.HeadUtil;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import ch.njol.skript.aliases.ItemType;
import org.bukkit.block.Block;

public class EffSetTexture extends Effect {
    private Expression<?> itemOrBlock;
    private Expression<String> textureString;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        itemOrBlock = exprs[0];
        textureString = (Expression<String>) exprs[1];
        return true;
    }

    @Override
    protected void execute(Event event) {
        Object target = itemOrBlock.getSingle(event);
        String tex = textureString.getSingle(event);
        if (tex == null || target == null) return;

        if (target instanceof ItemStack item) {
            HeadUtil.applyTexture(item, tex);
        } else if (target instanceof Block block) {
            HeadUtil.applyTexture(block, tex);
        } else if (target instanceof ItemType type) {
            ItemStack item = type.createItemStack(1);
            HeadUtil.applyTexture(item, tex);
        }
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "set texture of " + itemOrBlock.toString(event, debug) + " to " + textureString.toString(event, debug);
    }
}
