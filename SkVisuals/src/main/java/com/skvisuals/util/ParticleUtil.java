package com.skvisuals.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;

public final class ParticleUtil {

    /**
     * Spawn particle with support for all 1.21.11 data types.
     */
    public static void spawn(Particle particle, Location loc, int count, double ox, double oy, double oz, double speed, Object data, Collection<Player> viewers) {
        if (viewers == null) {
            loc.getWorld().spawnParticle(particle, loc, count, ox, oy, oz, speed, data);
        } else {
            for (Player p : viewers) {
                p.spawnParticle(particle, loc, count, ox, oy, oz, speed, data);
            }
        }
    }

    /**
     * Rotate a 3D vector using Euler angles (pitch, yaw, roll).
     */
    public static Vector rotate(Vector v, double pitch, double yaw, double roll) {
        double radPitch = Math.toRadians(pitch);
        double radYaw = Math.toRadians(yaw);
        double radRoll = Math.toRadians(roll);

        // Rotation matrices
        // Y-axis (Yaw)
        double yx = v.getX() * Math.cos(radYaw) + v.getZ() * Math.sin(radYaw);
        double yz = -v.getX() * Math.sin(radYaw) + v.getZ() * Math.cos(radYaw);
        v.setX(yx); v.setZ(yz);

        // X-axis (Pitch)
        double py = v.getY() * Math.cos(radPitch) - v.getZ() * Math.sin(radPitch);
        double pz = v.getY() * Math.sin(radPitch) + v.getZ() * Math.cos(radPitch);
        v.setY(py); v.setZ(pz);

        // Z-axis (Roll)
        double rx = v.getX() * Math.cos(radRoll) - v.getY() * Math.sin(radRoll);
        double ry = v.getX() * Math.sin(radRoll) + v.getY() * Math.cos(radRoll);
        v.setX(rx); v.setY(ry);

        return v;
    }
}
