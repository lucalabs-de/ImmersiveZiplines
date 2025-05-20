package de.lucalabs.ziplines.curves;

import net.minecraft.util.math.Vec3d;

public interface SegmentView {
    int getIndex();

    float getX(final float t);

    float getY(final float t);

    float getZ(final float t);

    Vec3d getPos(final float t);

    float getYaw();

    float getPitch();

    float getLength();
}
