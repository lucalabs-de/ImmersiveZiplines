package de.lucalabs.ziplines.utils;

import de.lucalabs.ziplines.curves.Catenary;
import net.minecraft.util.math.Vec3d;

public interface ZiplineUser {
    boolean immersiveZiplines$isUsingZipline();
    void immersiveZiplines$startUsingZipline(Catenary c, Vec3d anchor);
    void immersiveZiplines$stopUsingZipline();
}
