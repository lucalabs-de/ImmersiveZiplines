package de.lucalabs.ziplines.blocks.entity;

import de.lucalabs.ziplines.blocks.FastenerBlock;
import de.lucalabs.ziplines.components.ZiplineComponents;
import de.lucalabs.ziplines.fastener.Fastener;
import de.lucalabs.ziplines.registry.ZiplineBlockEntities;
import de.lucalabs.ziplines.registry.ZiplineBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

public final class FastenerBlockEntity extends BlockEntity {
    public FastenerBlockEntity(final BlockPos pos, final BlockState state) {
        super(ZiplineBlockEntities.FASTENER, pos, state);
    }

    // TODO check if this is ever needed
//    @Override
//    public Box getRenderBoundingBox() {
//        return this.getFastener().map(fastener -> fastener.getBounds().expand(1)).orElseGet(() -> ForgeRendering.getRenderBoundingBox(this));
//    }

    public static void tick(World world, BlockPos pos, BlockState state, FastenerBlockEntity be) {
        be.getFastener().ifPresent(fastener -> {
            if (!world.isClient() && fastener.hasNoConnections()) {
                world.removeBlock(pos, false);
            } else if (!world.isClient() && fastener.update()) {
                be.markDirty();
                world.updateListeners(pos, state, state, 3);
            }
        });
    }

    public static void tickClient(World level, BlockPos pos, BlockState state, FastenerBlockEntity be) {
        be.getFastener().ifPresent(Fastener::update);
    }

    public Vec3d getOffset() {
        return ZiplineBlocks.FASTENER.getOffset(this.getFacing(), 0.125F);
    }

    public Direction getFacing() {
        final BlockState state = this.world.getBlockState(this.pos);
        if (state.getBlock() != ZiplineBlocks.FASTENER) {
            return Direction.UP;
        }
        return state.get(FastenerBlock.FACING);
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }

    @Override
    public void setWorld(final World world) {
        super.setWorld(world);
        this.getFastener().ifPresent(fastener -> fastener.setWorld(world));
    }

    private Optional<Fastener<?>> getFastener() {
        return ZiplineComponents.FASTENER.get(this).get();
    }
}
