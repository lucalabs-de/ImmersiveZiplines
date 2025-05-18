package de.lucalabs.ziplines.fastener.accessor;

import de.lucalabs.ziplines.components.ZiplineComponents;
import de.lucalabs.ziplines.fastener.BlockFastener;
import de.lucalabs.ziplines.fastener.Fastener;
import de.lucalabs.ziplines.fastener.FastenerType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class BlockFastenerAccessor implements FastenerAccessor {
    private BlockPos pos = BlockPos.ORIGIN;

    public BlockFastenerAccessor() {
    }

    public BlockFastenerAccessor(final BlockFastener fastener) {
        this(fastener.getPos());
    }

    public BlockFastenerAccessor(final BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public Optional<Fastener<?>> get(final World world, final boolean load) {
        if (load || world.canSetBlock(this.pos)) {
            final BlockEntity entity = world.getBlockEntity(this.pos);
            if (entity != null) {
                return ZiplineComponents.FASTENER.get(entity).get();
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isGone(final World world) {
        if (world.isClient() || !world.canSetBlock(this.pos)) return false;
        final BlockEntity entity = world.getBlockEntity(this.pos);
        return entity == null || ZiplineComponents.FASTENER.get(entity).isEmpty();
    }

    @Override
    public FastenerType getType() {
        return FastenerType.BLOCK;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof BlockFastenerAccessor) {
            return this.pos.equals(((BlockFastenerAccessor) obj).pos);
        }
        return false;
    }

    @Override
    public NbtCompound serialize() {
        return NbtHelper.fromBlockPos(this.pos);
    }

    @Override
    public void deserialize(final NbtCompound nbt) {
        this.pos = NbtHelper.toBlockPos(nbt);
    }
}
