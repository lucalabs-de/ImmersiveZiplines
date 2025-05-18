package de.lucalabs.ziplines.hitbox;

import de.lucalabs.ziplines.utils.IntIdentifiable;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public record Intersection(Vec3d result, Box hitBox, IntIdentifiable subject) {
}
