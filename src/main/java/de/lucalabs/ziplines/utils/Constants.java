package de.lucalabs.ziplines.utils;

import net.minecraft.util.math.Box;

public final class Constants {

    public static final Box INFINITE_BOX = new Box(
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY);

    private Constants() {}
}
