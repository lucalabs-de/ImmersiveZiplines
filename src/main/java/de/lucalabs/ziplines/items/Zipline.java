package de.lucalabs.ziplines.items;

import de.lucalabs.ziplines.blocks.FastenerBlock;
import de.lucalabs.ziplines.components.ZiplineComponents;
import de.lucalabs.ziplines.connection.Connection;
import de.lucalabs.ziplines.entity.FenceFastenerEntity;
import de.lucalabs.ziplines.fastener.Fastener;
import de.lucalabs.ziplines.registry.ZiplineBlocks;
import de.lucalabs.ziplines.registry.ZiplineSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

public class Zipline extends Item {

    public Zipline(final Item.Settings properties) {
        super(properties);
    }

    @SuppressWarnings("deprecation")
    public static boolean isFence(final BlockState state) {
        return state.isSolid() && state.isIn(BlockTags.FENCES);
    }

    @Override
    public ActionResult useOnBlock(final ItemUsageContext context) {
        final PlayerEntity user = context.getPlayer();
        if (user == null) {
            return super.useOnBlock(context);
        }
        final World world = context.getWorld();
        final Direction side = context.getSide();
        final BlockPos clickPos = context.getBlockPos();
        final Block fastener = ZiplineBlocks.FASTENER;
        final ItemStack stack = context.getStack();
        if (this.isConnectionInOtherHand(user, stack)) {
            return ActionResult.PASS;
        }
        final BlockState fastenerState = fastener.getDefaultState().with(FastenerBlock.FACING, side);
        final BlockState currentBlockState = world.getBlockState(clickPos);
        final ItemPlacementContext blockContext = new ItemPlacementContext(context);
        final BlockPos placePos = blockContext.getBlockPos();
        if (currentBlockState.getBlock() == fastener) {
            if (!world.isClient()) {
                this.connect(stack, user, world, clickPos);
            }
            return ActionResult.SUCCESS;
        } else if (blockContext.canPlace() && fastenerState.canPlaceAt(world, placePos)) {
            if (!world.isClient()) {
                this.connect(stack, user, world, placePos, fastenerState);
            }
            return ActionResult.SUCCESS;
        } else if (isFence(currentBlockState)) {
            final AbstractDecorationEntity entity = FenceFastenerEntity.findHanging(world, clickPos);
            if (entity == null || entity instanceof FenceFastenerEntity) {
                if (!world.isClient()) {
                    this.connectFence(stack, user, world, clickPos, (FenceFastenerEntity) entity);
                }
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    private boolean isConnectionInOtherHand(final PlayerEntity user, final ItemStack stack) {
        final Fastener<?> attacher = ZiplineComponents.FASTENER.get(user).get().orElseThrow(IllegalStateException::new);
        return attacher.getFirstConnection().filter(connection -> stack.hasNbt()).isPresent();
    }

    private void connect(final ItemStack stack, final PlayerEntity user, final World world, final BlockPos pos) {
        final BlockEntity entity = world.getBlockEntity(pos);
        if (entity != null) {
            ZiplineComponents.FASTENER.get(entity).get().ifPresent(fastener -> this.connect(stack, user, world, fastener));
        }
    }

    private void connect(final ItemStack stack, final PlayerEntity user, final World world, final BlockPos pos, final BlockState state) {
        if (world.setBlockState(pos, state, 3)) {
            state.getBlock().onPlaced(world, pos, state, user, stack);
            final BlockSoundGroup sound = state.getBlock().getSoundGroup(state);
            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    sound.getPlaceSound(),
                    SoundCategory.BLOCKS,
                    (sound.getVolume() + 1) / 2,
                    sound.getPitch() * 0.8F
            );
            final BlockEntity entity = world.getBlockEntity(pos);
            if (entity != null) {
                ZiplineComponents.FASTENER.get(entity).get()
                        .ifPresent(destination -> this.connect(stack, user, world, destination, false));
            }
        }
    }

    public void connect(final ItemStack stack, final PlayerEntity user, final World world, final Fastener<?> fastener) {
        this.connect(stack, user, world, fastener, true);
    }

    public void connect(final ItemStack stack, final PlayerEntity user, final World world, final Fastener<?> fastener, final boolean playConnectSound) {
        ZiplineComponents.FASTENER.get(user).get().ifPresent(attacher -> {
            boolean playSound = playConnectSound;
            final Optional<Connection> placing = attacher.getFirstConnection();
            if (placing.isPresent()) {
                final Connection conn = placing.get();
                if (conn.reconnect(fastener)) {
                    stack.decrement(1);
                } else {
                    playSound = false;
                }
            } else {
                fastener.connect(world, attacher, false);
            }
            if (playSound) {
                final Vec3d pos = fastener.getConnectionPoint();
                world.playSound(null, pos.x, pos.y, pos.z, ZiplineSounds.CORD_CONNECT, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        });
    }

    private void connectFence(
            final ItemStack stack,
            final PlayerEntity user,
            final World world,
            final BlockPos pos,
            FenceFastenerEntity fastener) {
        final boolean playConnectSound;
        if (fastener == null) {
            fastener = FenceFastenerEntity.create(world, pos);
            playConnectSound = false;
        } else {
            playConnectSound = true;
        }

        this.connect(
                stack,
                user,
                world,
                ZiplineComponents.FASTENER.get(fastener).get().orElseThrow(IllegalStateException::new),
                playConnectSound);
    }
}


