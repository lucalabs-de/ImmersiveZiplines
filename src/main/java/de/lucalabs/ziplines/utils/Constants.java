package de.lucalabs.ziplines.utils;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class Constants {

    public static final Box INFINITE_BOX = new Box(
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY);

    public static final Vec3d DOWN = Vec3d.of(Direction.DOWN.getVector());
    public static final double DRAG = 0.01;

    private Constants() {}
}
