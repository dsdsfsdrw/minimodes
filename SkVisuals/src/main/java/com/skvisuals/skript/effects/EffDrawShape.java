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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class EffDrawShape extends Effect {
    private Expression<String> shapeName;
    private Expression<String> particleName;
    private Expression<Location> location;
    private Expression<Double> radius, height, pitch, yaw, roll, size;
    private Expression<Integer> points;
    private Expression<Color> color;
    private Expression<Player> players;
    private Expression<Double> viewRadius;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        shapeName = (Expression<String>) exprs[0];
        particleName = (Expression<String>) exprs[1];
        location = (Expression<Location>) exprs[2];
        radius = (Expression<Double>) exprs[3];
        points = (Expression<Integer>) exprs[4];
        height = (Expression<Double>) exprs[5];
        pitch = (Expression<Double>) exprs[6];
        yaw = (Expression<Double>) exprs[7];
        roll = (Expression<Double>) exprs[8];
        color = (Expression<Color>) exprs[9];
        size = (Expression<Double>) exprs[10];
        players = (Expression<Player>) exprs[11];
        viewRadius = (Expression<Double>) exprs[12];
        return true;
    }

    @Override
    protected void execute(Event event) {
        String shape = shapeName.getSingle(event);
        String name = particleName.getSingle(event);
        Location loc = location.getSingle(event);
        Double r = radius.getSingle(event);
        if (name == null || loc == null || r == null) return;

        Particle particle = ParticleUtil.parse(name);
        if (particle == null) return;

        Integer p = points != null ? points.getSingle(event) : null;
        Double h = height != null ? height.getSingle(event) : null;
        double dPitch = pitch != null && pitch.getSingle(event) != null ? pitch.getSingle(event) : 0;
        double dYaw = yaw != null && yaw.getSingle(event) != null ? yaw.getSingle(event) : 0;
        double dRoll = roll != null && roll.getSingle(event) != null ? roll.getSingle(event) : 0;

        int max = SkVisuals.getInstance() != null
                ? SkVisuals.getInstance().getConfig().getInt("max-particle-points", 500) : 500;

        Object data = null;
        if (color != null && color.getSingle(event) != null) {
            float s = size != null && size.getSingle(event) != null ? size.getSingle(event).floatValue() : 1.0f;
            if (particle == Particle.ENTITY_EFFECT) {
                data = color.getSingle(event);
            } else {
                data = new Particle.DustOptions(color.getSingle(event), s);
            }
        }

        Collection<Player> viewers = resolveViewers(event, loc);
        ParticleUtil.drawShape(particle, loc, shape, r, p != null ? p : 60, h != null ? h : 0,
                dPitch, dYaw, dRoll, data, viewers, max);
    }

    private Collection<Player> resolveViewers(Event event, Location loc) {
        if (players != null) {
            Player[] arr = players.getArray(event);
            if (arr != null) return Arrays.asList(arr);
        }
        if (viewRadius != null && viewRadius.getSingle(event) != null && loc.getWorld() != null) {
            double r = viewRadius.getSingle(event);
            List<Player> list = new ArrayList<>(loc.getWorld().getPlayers());
            list.removeIf(pl -> pl.getLocation().distanceSquared(loc) > r * r);
            return list;
        }
        return null;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "draw particle shape " + shapeName.toString(event, debug) + " with " + particleName.toString(event, debug);
    }
}
