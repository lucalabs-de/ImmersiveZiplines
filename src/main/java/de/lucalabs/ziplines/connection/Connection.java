package de.lucalabs.ziplines.connection;

import de.lucalabs.ziplines.curves.Catenary;
import de.lucalabs.ziplines.curves.CubicBezier;
import de.lucalabs.ziplines.curves.Catenary;
import de.lucalabs.ziplines.curves.SegmentIterator;
import de.lucalabs.ziplines.fastener.Fastener;
import de.lucalabs.ziplines.fastener.FastenerType;
import de.lucalabs.ziplines.fastener.accessor.FastenerAccessor;
import de.lucalabs.ziplines.hitbox.Hitbox;
import de.lucalabs.ziplines.hitbox.HitboxList;
import de.lucalabs.ziplines.hitbox.HitboxTree;
import de.lucalabs.ziplines.registry.ZiplineItems;
import de.lucalabs.ziplines.registry.ZiplineSounds;
import de.lucalabs.ziplines.utils.IntIdentifiable;
import de.lucalabs.ziplines.utils.NbtSerializable;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Connection implements NbtSerializable {
    public static final int MAX_LENGTH = 32;
    public static final double PULL_RANGE = 5;
    public static final float MAX_SLACK = 3;

    private static final CubicBezier SLACK_CURVE = new CubicBezier(0.495F, 0.505F, 0.495F, 0.505F);

    protected final Fastener<?> fastener;
    private final UUID uuid;
    protected World world;
    protected float slack = 1;
    protected Catenary prevCatenary;
    @Nullable
    private Catenary catenary;
    private FastenerAccessor destination;
    private Hitbox hitbox = Hitbox.empty();
    private int prevStretchStage;

    private boolean updateCatenary;
    private boolean removed;
    private boolean drop;

    public Connection(final World world, final Fastener<?> fastener, final UUID uuid) {
        this.world = world;
        this.fastener = fastener;
        this.uuid = uuid;
        this.computeCatenary();
    }

    @Nullable
    public final Catenary getCatenary() {
        return this.catenary;
    }

    @Nullable
    public final Catenary getPrevCatenary() {
        return this.prevCatenary == null ? this.catenary : this.prevCatenary;
    }

    public final World getWorld() {
        return this.world;
    }

    public void setWorld(final World world) {
        this.world = world;
    }

    public Hitbox getHitbox() {
        return this.hitbox;
    }

    public final Fastener<?> getFastener() {
        return this.fastener;
    }

    public final UUID getUUID() {
        return this.uuid;
    }

    public final FastenerAccessor getDestination() {
        return this.destination;
    }

    public final void setDestination(final Fastener<?> destination) {
        this.destination = destination.createAccessor();
        this.computeCatenary();
    }

    public boolean isDestination(final FastenerAccessor location) {
        return this.destination.equals(location);
    }

    public void setDrop() {
        this.drop = true;
    }

    public void noDrop() {
        this.drop = false;
    }

    public boolean shouldDrop() {
        return this.drop;
    }

    public ItemStack getItemStack() {
        return new ItemStack(ZiplineItems.ZIPLINE);
    }

    public float getRadius() {
        return 0.0625F;
    }

    public final boolean isDynamic() {
        return this.fastener.isMoving() || this.destination.get(this.world, false).filter(Fastener::isMoving).isPresent();
    }

    public final boolean isModifiable(final PlayerEntity player) {
        return this.world.canPlayerModifyAt(player, this.fastener.getPos());
    }

    public final void remove() {
        if (!this.removed) {
            this.removed = true;
        }
    }

    public final boolean isRemoved() {
        return this.removed;
    }

    public void computeCatenary() {
        this.updateCatenary = true;
    }

    public void initialize(final Fastener<?> destination, final boolean drop) {
        this.destination = destination.createAccessor();
        this.drop = drop;
    }

    public void disconnect(final PlayerEntity player, final Vec3d hit) {
        this.destination.get(this.world).ifPresent(f -> this.disconnect(f, hit));
    }

    private void disconnect(final Fastener<?> destinationFastener, final Vec3d hit) {
        this.fastener.removeConnection(this);
        destinationFastener.removeConnection(this.uuid);
        if (this.shouldDrop()) {
            final ItemStack stack = this.getItemStack();
            final ItemEntity item = new ItemEntity(this.world, hit.x, hit.y, hit.z, stack);
            final float scale = 0.05F;
            item.setVelocity(
                    this.world.random.nextGaussian() * scale,
                    this.world.random.nextGaussian() * scale + 0.2F,
                    this.world.random.nextGaussian() * scale
            );
            this.world.spawnEntity(item);
        }
        this.world.playSound(null, hit.x, hit.y, hit.z, ZiplineSounds.CORD_DISCONNECT, SoundCategory.BLOCKS, 1, 1);
    }

    public boolean reconnect(final Fastener<?> destination) {
        return this.fastener.reconnect(this.world, this, destination);
    }

    public boolean interact(
            final PlayerEntity player,
            final Vec3d hit,
            final int feature,
            final ItemStack heldStack,
            final Hand hand) {

        if (heldStack.isOf(Items.STRING)) {
            this.slacken(hit, 0.2F);
            return true;
        } else if (heldStack.isOf(Items.STICK)) {
            this.slacken(hit, -0.2F);
            return true;
        } else if (heldStack.isOf(Items.BOW)) {
            // TODO start ziplining!
        }
        return false;
    }

    public boolean matches(final ItemStack stack) {
        return ZiplineItems.ZIPLINE.equals(stack.getItem());
    }

    private void slacken(final Vec3d hit, final float amount) {
        if (this.slack <= 0 && amount < 0 || this.slack >= MAX_SLACK && amount > 0) {
            return;
        }
        this.slack = MathHelper.clamp(this.slack + amount, 0, MAX_SLACK);
        if (this.slack < 1e-2F) {
            this.slack = 0;
        }
        this.computeCatenary();
        this.world.playSound(null, hit.x, hit.y, hit.z, ZiplineSounds.CORD_STRETCH, SoundCategory.BLOCKS, 1, 0.8F + (MAX_SLACK - this.slack) * 0.4F);
    }

    public final boolean update(final Vec3d from) {
        this.prevCatenary = this.catenary;
        final boolean changed = this.destination.get(this.world, false).map(dest -> {
            final Vec3d point = dest.getConnectionPoint();
            final boolean c = this.updateCatenary(from, point);
            final double dist = point.distanceTo(from);
            final double pull = dist - MAX_LENGTH + PULL_RANGE;
            if (pull > 0) {
                final int stage = (int) (pull + 0.1F);
                if (stage > this.prevStretchStage) {
                    this.world.playSound(null, point.x, point.y, point.z, ZiplineSounds.CORD_STRETCH, SoundCategory.BLOCKS, 0.25F, 0.5F + stage / 8F);
                }
                this.prevStretchStage = stage;
            }
            if (dist > MAX_LENGTH + PULL_RANGE) {
                this.world.playSound(null, point.x, point.y, point.z, ZiplineSounds.CORD_SNAP, SoundCategory.BLOCKS, 0.75F, 0.8F + this.world.random.nextFloat() * 0.3F);
                this.remove();
            } else if (dest.isMoving()) {
                dest.resistSnap(from);
            }
            return c;
        }).orElse(false);

        if (this.destination.isGone(this.world)) {
            this.remove();
        }

        return changed;
    }

    private boolean updateCatenary(final Vec3d from, final Vec3d point) {
        if (this.updateCatenary || this.isDynamic()) {
            final Vec3d vec = point.subtract(from);
            if (vec.length() > 1e-6) {
                final Direction facing = this.fastener.getFacing();
                this.catenary = Catenary.from(
                        vec,
                        facing.getAxis() == Direction.Axis.Y
                                ? 0.0F
                                : (float) Math.toRadians(90.0F + facing.asRotation()),
                        SLACK_CURVE,
                        this.slack);
                final HitboxList.Builder bob = new HitboxList.Builder();
                this.addCollision(bob, from);
                this.hitbox = bob.build();
            }
            this.updateCatenary = false;
            return true;
        }
        return false;
    }

    public void addCollision(final HitboxList.Builder collision, final Vec3d origin) {
        if (this.catenary == null) {
            return;
        }
        final int count = this.catenary.getCount();
        if (count <= 2) {
            return;
        }
        final float r = this.getRadius();
        final SegmentIterator it = this.catenary.iterator();
        final Box[] bounds = new Box[count - 1];
        int index = 0;
        while (it.next()) {
            final float x0 = it.getX(0.0F);
            final float y0 = it.getY(0.0F);
            final float z0 = it.getZ(0.0F);
            final float x1 = it.getX(1.0F);
            final float y1 = it.getY(1.0F);
            final float z1 = it.getZ(1.0F);
            bounds[index++] = new Box(
                    origin.x + x0, origin.y + y0, origin.z + z0,
                    origin.x + x1, origin.y + y1, origin.z + z1
            ).expand(r);
        }
        collision.add(HitboxTree.build(i -> Segment.INSTANCE, i -> bounds[i], 1, bounds.length - 2));
    }

    @Override
    public NbtCompound serialize() {
        final NbtCompound compound = new NbtCompound();
        compound.put("destination", FastenerType.serialize(this.destination));
        compound.putFloat("slack", this.slack);
        if (!this.drop) compound.putBoolean("drop", false);
        return compound;
    }

    @Override
    public void deserialize(final NbtCompound compound) {
        this.destination = FastenerType.deserialize(compound.getCompound("destination"));
        this.slack = compound.contains("slack", NbtElement.NUMBER_TYPE) ? compound.getFloat("slack") : 1;
        this.drop = !compound.contains("drop", NbtElement.NUMBER_TYPE) || compound.getBoolean("drop");
        this.updateCatenary = true;
    }

    public static final class Segment implements IntIdentifiable {
        public static final Segment INSTANCE = new Segment();

        private Segment() {
        }

        @Override
        public int getId() {
            return 0;
        }
    }
}
