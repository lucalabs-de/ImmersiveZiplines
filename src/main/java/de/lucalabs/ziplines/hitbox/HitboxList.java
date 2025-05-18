package de.lucalabs.ziplines.hitbox;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class HitboxList implements Hitbox {
    private final ImmutableList<Hitbox> collision;

    private HitboxList(final Builder builder) {
        this.collision = builder.collision.build();
    }

    @Nullable
    @Override
    public Intersection intersect(final Vec3d origin, final Vec3d end) {
        Intersection result = null;
        double distance = Double.MAX_VALUE;
        for (final Hitbox collidable : this.collision) {
            final Intersection r = collidable.intersect(origin, end);
            if (r != null) {
                final double d = r.result().distanceTo(origin);
                if (d < distance) {
                    result = r;
                    distance = d;
                }
            }
        }
        return result;
    }

    public static class Builder {
        final ImmutableList.Builder<Hitbox> collision = new ImmutableList.Builder<>();

        public Builder add(final Hitbox collidable) {
            this.collision.add(collidable);
            return this;
        }

        public HitboxList build() {
            return new HitboxList(this);
        }
    }
}
