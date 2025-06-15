package de.lucalabs.ziplines.curves;

import de.lucalabs.ziplines.connection.Connection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;

public final class Catenary implements Iterable<SegmentView> {
    private static final int MIN_VERTEX_COUNT = 8;

    private final int count;

    private final float yaw;

    private final float dx;

    private final float dz;

    private final float[] x;

    private final float[] y;

    private final float length;

    private Catenary(final int count, final float yaw, final float dx, final float dz, final float[] x, final float[] y, final float length) {
        this.count = count;
        this.yaw = yaw;
        this.dx = dx;
        this.dz = dz;
        this.x = x;
        this.y = y;
        this.length = length;
    }

    public static Catenary from(final Vec3d direction, final float verticalYaw, final CubicBezier bezier, final float slack) {
        final float dist = (float) direction.length();
        final float length;
        if (slack < 1e-2 || Math.abs(direction.x) < 1e-6 && Math.abs(direction.z) < 1e-6) {
            length = dist;
        } else {
            length = dist + (lengthFunc(bezier, dist) - dist) * slack;
        }
        return from(direction, verticalYaw, length);
    }

    private static float lengthFunc(final CubicBezier bezier, final double length) {
        return bezier.eval(MathHelper.clamp((float) length / Connection.MAX_LENGTH, 0, 1)) * Connection.MAX_LENGTH;
    }

    public static Catenary from(final Vec3d dir, final float verticalYaw, final float ropeLength) {
        final float endX = MathHelper.sqrt((float) (dir.x * dir.x + dir.z * dir.z));
        final float endY = (float) dir.y;
        final float angle = endX < 1e-3F ? endY < 0.0F ? verticalYaw + MathHelper.PI : verticalYaw : (float) MathHelper.atan2(dir.z, dir.x);
        final float vx = MathHelper.cos(angle);
        final float vz = MathHelper.sin(angle);
        if (dir.length() > 2.0F * Connection.MAX_LENGTH) {
            return new Catenary(2, angle, vx, vz, new float[]{0.0F, endX}, new float[]{0.0F, endY}, MathHelper.sqrt(endX * endX + endY * endY));
        }
        final int count = Math.max((int) (ropeLength * CatenaryUtils.SEG_LENGTH), MIN_VERTEX_COUNT);
        final float[] x = new float[count];
        final float[] y = new float[count];
        CatenaryUtils.catenary(0.0F, 0.0F, endX, endY, ropeLength, count, x, y);
        float length = 0.0F;
        for (int i = 1; i < count; i++) {
            final float dx = x[i] - x[i - 1];
            final float dy = y[i] - y[i - 1];
            length += MathHelper.sqrt(dx * dx + dy * dy);
        }
        return new Catenary(count, angle, vx, vz, x, y, length);
    }

    public int getCount() {
        return this.count;
    }

    public float getX() {
        return this.x[this.count - 1] * this.dx;
    }

    public float getY() {
        return this.y[this.count - 1];
    }

    public float getZ() {
        return this.x[this.count - 1] * this.dz;
    }

    public float getX(final int i) {
        return this.x[i] * this.dx;
    }

    public float getX(final int i, final float t) {
        return MathHelper.lerp(t, this.x[i], this.x[i + 1]) * this.dx;
    }

    public float getY(final int i) {
        return this.y[i];
    }

    public float getY(final int i, final float t) {
        return MathHelper.lerp(t, this.y[i], this.y[i + 1]);
    }

    public float getZ(final int i) {
        return this.x[i] * this.dz;
    }

    public float getZ(final int i, final float t) {
        return MathHelper.lerp(t, this.x[i], this.x[i + 1]) * this.dz;
    }

    public float getDx(final int i) {
        return (this.x[i + 1] - this.x[i]) * this.dx;
    }

    public float getDy(final int i) {
        return (this.y[i + 1] - this.y[i]);
    }

    public float getDz(final int i) {
        return (this.x[i + 1] - this.x[i]) * this.dz;
    }

    public float getLength() {
        return this.length;
    }

    public Vec3d getT(float t) {
        return atTSegmentDo(t, SegmentView::getPos);
    }

    public Vec3d getTangentAtT(float t) {
        return atTSegmentDo(t, (seg, x) -> seg.getPos(1).subtract(seg.getPos(0)).normalize()); // TODO also an avoidable sqrt
    }

    private <T> T atTSegmentDo(float t, BiFunction<SegmentView, Float, T> f) {
        if (t == 0) {
            return f.apply(getSegmentAtIndex(0), 0F);
        }

        if (t == 1) {
            return f.apply(getSegmentAtIndex(count - 2), 1F);
        }

        float distanceFrom0 = getLength() * t;
        float totalLengthTraveled = 0;
        for (SegmentView segment : this) {
            float segLength = segment.getLength();
            if (totalLengthTraveled + segLength >= distanceFrom0) {
                float distanceInSegment = distanceFrom0 - totalLengthTraveled;
                float t2 = distanceInSegment / segLength;
                return f.apply(segment, t2);
            } else {
                totalLengthTraveled += segLength;
            }
        }

        throw new AssertionError("this should never happen");
    }

    public PosInSegment snapToCurve(Vec3d pos) {
        double minSqDist = Integer.MAX_VALUE;
        Vec3d closestPoint = Vec3d.ZERO;
        SegmentView closestSegment = null;
        float currentOffsetFrom0 = 0;
        float closestPointOffsetFrom0 = 0;

        for (SegmentView seg : this) {
            Vec3d toPoint = pos.subtract(seg.getPos(0));
            Vec3d segment = seg.getPos(1).subtract(seg.getPos(0));
            float closestSegmentT = MathHelper.clamp((float) (toPoint.dotProduct(segment) / segment.lengthSquared()), 0, 1);
            double sqDist = seg.getPos(closestSegmentT).subtract(pos).lengthSquared();

            currentOffsetFrom0 += seg.getLength();

            if (sqDist < minSqDist) {
                minSqDist = sqDist;
                closestPoint = seg.getPos(closestSegmentT);
                closestSegment = seg;
                closestPointOffsetFrom0 = currentOffsetFrom0;
            }
        }

        return new PosInSegment(closestPoint, closestSegment, closestPointOffsetFrom0 / getLength());
    }

    @Override
    @NotNull
    public Iterator<SegmentView> iterator() {
        return new CatenarySegmentIterator();
    }

    public Catenary lerp(final Catenary curve, final float delta) {
        if (this == curve) {
            return this;
        }
        if (this.count > curve.count) {
            return curve.lerp(this, 1.0F - delta);
        }
        final float[] nx = new float[this.count];
        final float[] ny = new float[this.count];
        for (int i = 0; i < this.count; i++) {
            final boolean end = this.count != curve.count && i == this.count - 1;
            nx[i] = MathHelper.lerp(delta, this.x[i], curve.x[end ? curve.count - 1 : i]);
            ny[i] = MathHelper.lerp(delta, this.y[i], curve.y[end ? curve.count - 1 : i]);
        }
        final float angle = de.lucalabs.ziplines.utils.MathHelper.lerpAngle(this.yaw, curve.yaw, delta);
        final float vx = MathHelper.cos(angle);
        final float vz = MathHelper.sin(angle);
        return new Catenary(this.count, angle, vx, vz, nx, ny, MathHelper.lerp(delta, this.length, curve.length));
    }


    private SegmentView getSegmentAtIndex(int index) {
        return new SegmentView() {
            @Override
            public int getIndex() {
                return index;
            }

            @Override
            public float getX(float t) {
                if (t == 0.0F) {
                    return Catenary.this.getX(index);
                }
                if (t == 1.0F) {
                    return Catenary.this.getX(index + 1);
                }
                return Catenary.this.getX(index, t);
            }

            @Override
            public float getY(float t) {
                if (t == 0.0F) {
                    return Catenary.this.getY(index);
                }
                if (t == 1.0F) {
                    return Catenary.this.getY(index + 1);
                }

                return Catenary.this.getY(index, t);
            }

            @Override
            public float getZ(float t) {
                if (t == 0.0F) {
                    return Catenary.this.getZ(index);
                }
                if (t == 1.0F) {
                    return Catenary.this.getZ(index + 1);
                }
                return Catenary.this.getZ(index, t);
            }

            @Override
            public Vec3d getPos(float t) {
                return new Vec3d(getX(t), getY(t), getZ(t));
            }

            @Override
            public float getYaw() {
                return Catenary.this.yaw;
            }

            @Override
            public float getPitch() {
                return Catenary.this.getPitch(index);
            }

            @Override
            public float getLength() {
                return Catenary.this.getLength(index);
            }
        };
    }

    private float getPitch(int index) {
        final float dx = x[index + 1] - x[index];
        final float dy = y[index + 1] - y[index];
        return (float) MathHelper.atan2(dy, dx);
    }

    private float getLength(final int index) {
        final float dx = x[index + 1] - x[index];
        final float dy = y[index + 1] - y[index];
        return MathHelper.sqrt(dx * dx + dy * dy); // TODO precompute these lengths, this is really inefficent
    }

    public record PosInSegment(Vec3d pos, SegmentView segment, float t) {
    }

    private class CatenarySegmentIterator implements Iterator<SegmentView> {

        protected int index;

        public CatenarySegmentIterator() {
            this.index = -1;
        }

        public boolean hasNext() {
            return this.index + 2 < count;
        }

        @Override
        public SegmentView next() {
            final int nextIndex = this.index + 1;

            if (nextIndex >= count) {
                throw new NoSuchElementException();
            }

            this.index = nextIndex;

            return getSegmentAtIndex(nextIndex);
        }
    }
}
