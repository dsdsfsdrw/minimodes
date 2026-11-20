package com.skvisuals.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public final class ParticleUtil {

    private ParticleUtil() {}

    public static Particle parse(String name) {
        if (name == null) return null;
        String normalized = name.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        try {
            return Particle.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static void spawn(Particle particle, Location loc, int count, double ox, double oy, double oz,
                             double speed, Object data, Collection<Player> viewers) {
        if (particle == null || loc == null || loc.getWorld() == null) return;
        if (viewers == null || viewers.isEmpty()) {
            loc.getWorld().spawnParticle(particle, loc, Math.max(count, 1), ox, oy, oz, speed, data);
        } else {
            for (Player p : viewers) {
                if (p != null && p.isOnline()) {
                    p.spawnParticle(particle, loc, Math.max(count, 1), ox, oy, oz, speed, data);
                }
            }
        }
    }

    /** Rotate a vector by Euler angles in degrees: pitch (X), yaw (Y), roll (Z). */
    public static Vector rotate(Vector v, double pitch, double yaw, double roll) {
        double rp = Math.toRadians(pitch);
        double ry = Math.toRadians(yaw);
        double rr = Math.toRadians(roll);
        double x = v.getX(), y = v.getY(), z = v.getZ();

        // Yaw around Y axis
        double nx = x * Math.cos(ry) + z * Math.sin(ry);
        double nz = -x * Math.sin(ry) + z * Math.cos(ry);
        x = nx; z = nz;

        // Pitch around X axis
        double ny = y * Math.cos(rp) - z * Math.sin(rp);
        nz = y * Math.sin(rp) + z * Math.cos(rp);
        y = ny; z = nz;

        // Roll around Z axis
        nx = x * Math.cos(rr) - y * Math.sin(rr);
        ny = x * Math.sin(rr) + y * Math.cos(rr);
        x = nx; y = ny;

        return new Vector(x, y, z);
    }

    /** Build the raw (unrotated) point cloud for a shape, relative to its centre. */
    public static List<Vector> shapePoints(String shape, double radius, int points, double height) {
        List<Vector> out = new ArrayList<>();
        int n = Math.max(points, 3);
        String s = shape == null ? "circle" : shape.toLowerCase(Locale.ROOT).trim();

        switch (s) {
            case "ring", "circle" -> {
                for (int i = 0; i < n; i++) {
                    double a = 2 * Math.PI * i / n;
                    out.add(new Vector(Math.cos(a) * radius, 0, Math.sin(a) * radius));
                }
            }
            case "sphere" -> {
                int rings = Math.max(n / 4, 2);
                for (int r = 0; r <= rings; r++) {
                    double phi = Math.PI * r / rings;
                    for (int i = 0; i < n; i++) {
                        double theta = 2 * Math.PI * i / n;
                        out.add(new Vector(
                                radius * Math.sin(phi) * Math.cos(theta),
                                radius * Math.cos(phi),
                                radius * Math.sin(phi) * Math.sin(theta)));
                    }
                }
            }
            case "filled sphere" -> {
                int steps = Math.max(n / 6, 2);
                for (int i = 0; i < n * 2; i++) {
                    double u = Math.random();
                    double v = Math.random();
                    double rr2 = radius * Math.cbrt(u);
                    double theta = 2 * Math.PI * v;
                    double cosPhi = 2 * Math.random() - 1;
                    double sinPhi = Math.sqrt(1 - cosPhi * cosPhi);
                    out.add(new Vector(rr2 * sinPhi * Math.cos(theta), rr2 * cosPhi, rr2 * sinPhi * Math.sin(theta)));
                }
            }
            case "helix" -> {
                double h = height > 0 ? height : 2.0;
                for (int i = 0; i < n; i++) {
                    double t = (double) i / n;
                    double a = t * Math.PI * 6;
                    out.add(new Vector(Math.cos(a) * radius, t * h, Math.sin(a) * radius));
                }
            }
            case "spiral" -> {
                double h = height > 0 ? height : 1.0;
                for (int i = 0; i < n; i++) {
                    double t = (double) i / n;
                    double a = t * Math.PI * 8;
                    double r = radius * (1 - t);
                    out.add(new Vector(Math.cos(a) * r, t * h, Math.sin(a) * r));
                }
            }
            case "cube" -> {
                for (int face = 0; face < 6; face++) {
                    for (int i = 0; i < n; i++) {
                        double t = -radius + 2 * radius * i / (double) (n - 1);
                        switch (face) {
                            case 0 -> out.add(new Vector(radius, t, -radius + 2 * radius * i / (double) (n - 1)));
                            case 1 -> out.add(new Vector(-radius, t, -radius + 2 * radius * i / (double) (n - 1)));
                            case 2 -> out.add(new Vector(-radius + 2 * radius * i / (double) (n - 1), radius, t));
                            case 3 -> out.add(new Vector(-radius + 2 * radius * i / (double) (n - 1), -radius, t));
                            case 4 -> out.add(new Vector(t, -radius + 2 * radius * i / (double) (n - 1), radius));
                            default -> out.add(new Vector(t, -radius + 2 * radius * i / (double) (n - 1), -radius));
                        }
                    }
                }
            }
            case "star" -> {
                for (int i = 0; i < n; i++) {
                    double a = Math.PI * i / n * 2;
                    double r = (i % 2 == 0) ? radius : radius * 0.45;
                    out.add(new Vector(Math.cos(a) * r, 0, Math.sin(a) * r));
                }
                out.add(out.get(0));
            }
            default -> {
                for (int i = 0; i < n; i++) {
                    double a = 2 * Math.PI * i / n;
                    out.add(new Vector(Math.cos(a) * radius, 0, Math.sin(a) * radius));
                }
            }
        }
        return out;
    }

    /** Draw a rotated particle shape at the given location. */
    public static void drawShape(Particle particle, Location center, String shape, double radius, int points,
                                 double height, double pitch, double yaw, double roll, Object data,
                                 Collection<Player> viewers, int maxPoints) {
        if (particle == null || center == null) return;
        List<Vector> raw = shapePoints(shape, radius, Math.min(Math.max(points, 3), maxPoints), height);
        for (Vector v : raw) {
            Vector rotated = rotate(v.clone(), pitch, yaw, roll);
            Location point = center.clone().add(rotated);
            spawn(particle, point, 1, 0, 0, 0, 0, data, viewers);
        }
    }

    /** Draw a line, beam or sine wave between two points. */
    public static void drawLine(Particle particle, Location from, Location to, double step, double amplitude,
                                double frequency, boolean wave, Object data, Collection<Player> viewers, int maxPoints) {
        if (particle == null || from == null || to == null || from.getWorld() == null) return;
        Vector start = from.toVector();
        Vector delta = to.toVector().subtract(start);
        double length = delta.length();
        if (length <= 0.0001) return;

        double spacing = step > 0 ? step : 0.25;
        int count = (int) Math.min(Math.floor(length / spacing) + 1, maxPoints);
        if (count < 1) count = 1;

        Vector dir = delta.clone().normalize();
        // Perpendicular basis for the sine offset
        Vector up = Math.abs(dir.getY()) > 0.99 ? new Vector(1, 0, 0) : new Vector(0, 1, 0);
        Vector side = dir.clone().crossProduct(up).normalize();
        Vector side2 = dir.clone().crossProduct(side).normalize();

        for (int i = 0; i <= count; i++) {
            double t = (double) i / count;
            Vector point = start.clone().add(delta.clone().multiply(t));
            if (wave && amplitude > 0) {
                double offset = Math.sin(t * Math.PI * 2 * Math.max(frequency, 1)) * amplitude;
                point.add(side.clone().multiply(offset));
            }
            spawn(particle, point.toLocation(from.getWorld()), 1, 0, 0, 0, 0, data, viewers);
        }
    }
}
