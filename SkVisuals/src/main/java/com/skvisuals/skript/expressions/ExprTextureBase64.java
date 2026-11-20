package com.skvisuals.skript.expressions;

import com.skvisuals.util.HeadUtil;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class ExprTextureBase64 extends SimpleExpression<String> {
    private Expression<?> source;

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        source = exprs[0];
        return true;
    }

    @Override
    protected String[] get(Event event) {
        Optional<String> result = extract(source.getSingle(event));
        return result.map(s -> new String[]{s}).orElse(new String[0]);
    }

    private Optional<String> extract(Object o) {
        if (o instanceof Player p) return HeadUtil.extractBase64(p);
        if (o instanceof OfflinePlayer p) return HeadUtil.extractBase64(p);
        if (o instanceof ItemStack item) return HeadUtil.extractBase64(item);
        if (o instanceof ItemType type) return HeadUtil.extractBase64(type.getItem().clone());
        if (o instanceof Block block) return HeadUtil.extractBase64(block);
        return Optional.empty();
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "base64 texture of " + source.toString(event, debug);
    }
}
