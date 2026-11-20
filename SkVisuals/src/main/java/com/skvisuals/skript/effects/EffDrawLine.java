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

public class EffDrawLine extends Effect {
    private String type;
    private Expression<String> particleName;
    private Expression<Location> from, to;
    private Expression<Double> step, amplitude, frequency, size;
    private Expression<Color> color;
    private Expression<Player[]> players;
    private Expression<Double> viewRadius;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        type = parseResult.tags.size() > 0 ? parseResult.tags.get(0) : "line";
        particleName = (Expression<String>) exprs[0];
        from = (Expression<Location>) exprs[1];
        to = (Expression<Location>) exprs[2];
        step = (Expression<Double>) exprs[3];
        amplitude = (Expression<Double>) exprs[4];
        frequency = (Expression<Double>) exprs[5];
        color = (Expression<Color>) exprs[6];
        size = (Expression<Double>) exprs[7];
        players = (Expression<Player[]>) exprs[8];
        viewRadius = (Expression<Double>) exprs[9];
        return true;
    }

    @Override
    protected void execute(Event event) {
        String name = particleName.getSingle(event);
        Location a = from.getSingle(event);
        Location b = to.getSingle(event);
        if (name == null || a == null || b == null) return;

        Particle particle = ParticleUtil.parse(name);
        if (particle == null) return;

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

        double dStep = step != null && step.getSingle(event) != null ? step.getSingle(event) : 0.25;
        double dAmp = amplitude != null && amplitude.getSingle(event) != null ? amplitude.getSingle(event) : 1.0;
        double dFreq = frequency != null && frequency.getSingle(event) != null ? frequency.getSingle(event) : 2.0;

        Collection<Player> viewers = resolveViewers(event, a);
        ParticleUtil.drawLine(particle, a, b, dStep, dAmp, dFreq, "wave".equalsIgnoreCase(type), data, viewers, max);
    }

    private Collection<Player> resolveViewers(Event event, Location loc) {
        if (players != null) {
            Player[] arr = players.getArray(event);
            if (arr != null) return Arrays.asList(arr);
        }
        if (viewRadius != null && viewRadius.getSingle(event) != null && loc.getWorld() != null) {
            double r = viewRadius.getSingle(event);
            List<Player> list = new ArrayList<>(loc.getWorld().getPlayers());
            list.removeIf(p -> p.getLocation().distanceSquared(loc) > r * r);
            return list;
        }
        return null;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "draw particle " + type + " of " + particleName.toString(event, debug);
    }
}
