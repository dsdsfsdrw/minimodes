package com.skvisuals.skript.effects;

import com.skvisuals.SkVisuals;
import com.skvisuals.util.ParticleUtil;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class EffSpawnParticle extends Effect {
    private Expression<Integer> count;
    private Expression<String> particleName;
    private Expression<Location> location;
    private Expression<Double> offsetX, offsetY, offsetZ, speed, size;
    private Expression<Color> color, toColor;
    private Expression<Player[]> players;
    private Expression<Double> radius;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        count = (Expression<Integer>) exprs[0];
        particleName = (Expression<String>) exprs[1];
        location = (Expression<Location>) exprs[2];
        int i = 3;
        offsetX = (Expression<Double>) exprs[i++];
        offsetY = (Expression<Double>) exprs[i++];
        offsetZ = (Expression<Double>) exprs[i++];
        speed = (Expression<Double>) exprs[i++];
        color = (Expression<Color>) exprs[i++];
        toColor = (Expression<Color>) exprs[i++];
        size = (Expression<Double>) exprs[i++];
        i++; // material slot (unused for simple spawns)
        players = (Expression<Player[]>) exprs[i++];
        radius = (Expression<Double>) exprs[i];
        return true;
    }

    @Override
    protected void execute(Event event) {
        Integer c = count.getSingle(event);
        String name = particleName.getSingle(event);
        Location loc = location.getSingle(event);
        if (c == null || name == null || loc == null) return;

        Particle particle = ParticleUtil.parse(name);
        if (particle == null) return;

        int max = SkVisuals.getInstance() != null
                ? SkVisuals.getInstance().getConfig().getInt("max-particle-points", 500) : 500;
        int amount = Math.min(Math.max(c, 1), max);

        float dustSize = size != null && size.getSingle(event) != null ? size.getSingle(event).floatValue() : 1.0f;
        Object data = null;
        if (color != null && color.getSingle(event) != null) {
            Color col = color.getSingle(event);
            if (particle == Particle.DUST) {
                data = new Particle.DustOptions(col, dustSize);
            } else if (particle == Particle.DUST_COLOR_TRANSITION && toColor != null && toColor.getSingle(event) != null) {
                data = new Particle.DustTransition(col, toColor.getSingle(event), dustSize);
            } else if (particle == Particle.ENTITY_EFFECT) {
                data = col;
            }
        }

        Collection<Player> viewers = resolveViewers(event, loc);
        double ox = offsetX != null && offsetX.getSingle(event) != null ? offsetX.getSingle(event) : 0;
        double oy = offsetY != null && offsetY.getSingle(event) != null ? offsetY.getSingle(event) : 0;
        double oz = offsetZ != null && offsetZ.getSingle(event) != null ? offsetZ.getSingle(event) : 0;
        double spd = speed != null && speed.getSingle(event) != null ? speed.getSingle(event) : 0;

        ParticleUtil.spawn(particle, loc, amount, ox, oy, oz, spd, data, viewers);
    }

    private Collection<Player> resolveViewers(Event event, Location loc) {
        if (players != null) {
            Player[] arr = players.getArray(event);
            if (arr != null) return Arrays.asList(arr);
        }
        if (radius != null && radius.getSingle(event) != null && loc.getWorld() != null) {
            double r = radius.getSingle(event);
            List<Player> list = new java.util.ArrayList<>(loc.getWorld().getPlayers());
            list.removeIf(p -> p.getLocation().distanceSquared(loc) > r * r);
            return list;
        }
        return null;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "spawn particles " + particleName.toString(event, debug) + " at " + location.toString(event, debug);
    }
}
