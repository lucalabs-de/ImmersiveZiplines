package de.lucalabs.ziplines.hitbox;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public interface Hitbox {
    @Nullable
    Intersection intersect(final Vec3d origin, final Vec3d end);

    static Hitbox empty() {
        return (o, e) -> null;
    }
}
