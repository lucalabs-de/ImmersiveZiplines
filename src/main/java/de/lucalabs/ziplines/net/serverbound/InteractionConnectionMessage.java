package de.lucalabs.ziplines.net.serverbound;

import de.lucalabs.ziplines.ImmersiveZiplines;
import de.lucalabs.ziplines.connection.Connection;
import de.lucalabs.ziplines.connection.PlayerAction;
import de.lucalabs.ziplines.hitbox.Intersection;
import de.lucalabs.ziplines.net.ConnectionMessage;
import de.lucalabs.ziplines.utils.Utils;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class InteractionConnectionMessage extends ConnectionMessage {

    public static final Identifier ID = new Identifier(ImmersiveZiplines.MOD_ID, "interaction_connection");

    private static final float RANGE = (Connection.MAX_LENGTH + 1) * (Connection.MAX_LENGTH + 1);
    private static final float REACH = 6 * 6;

    public InteractionConnectionMessage(
            final Connection connection,
            final PlayerAction type,
            final Intersection intersection) {
        super(connection);

        Vec3d hit = intersection.result();

        writeByte(type.ordinal());
        writeDouble(hit.x);
        writeDouble(hit.y);
        writeDouble(hit.z);
        writeVarInt(intersection.subject().getId());
    }

    public static void apply(
            MinecraftServer server,
            ServerPlayerEntity player,
            ServerPlayNetworkHandler handler,
            PacketByteBuf buf,
            PacketSender responseSender) {
        ParsedData baseData = parse(buf);

        short type = buf.readUnsignedByte();
        PlayerAction action = Utils.getEnumValue(PlayerAction.class, type);

        double hitX = buf.readDouble();
        double hitY = buf.readDouble();
        double hitZ = buf.readDouble();
        Vec3d hit = new Vec3d(hitX, hitY, hitZ);

        int featureId = buf.readVarInt();

        server.execute(() -> {
            getConnection(baseData.accessor(), baseData.id(), c -> true, player.getWorld()).ifPresent(connection -> {
                if (connection.isModifiable(player) &&
                        player.squaredDistanceTo(Vec3d.of(connection.getFastener().getPos())) < RANGE &&
                        player.squaredDistanceTo(hitX, hitY, hitZ) < REACH
                ) {
                    if (action == PlayerAction.ATTACK) {
                        connection.disconnect(player, hit);
                    } else {
                        interact(player, connection, featureId, hit);
                    }
                }
            });
        });
    }

    private static void interact(
            final PlayerEntity player,
            final Connection connection,
            final int featureId,
            final Vec3d hit) {
        for (final Hand hand : Hand.values()) {
            final ItemStack stack = player.getStackInHand(hand);
            final ItemStack oldStack = stack.copy();
            if (connection.interact(player, hit, featureId, stack, hand)) {
                updateItem(player, oldStack, stack, hand);
                break;
            }
        }
    }

    private static void updateItem(
            final PlayerEntity player,
            final ItemStack oldStack,
            final ItemStack stack,
            final Hand hand) {
        if (stack.getCount() <= 0 && !player.getAbilities().creativeMode) {
//            ForgeEventFactory.onPlayerDestroyItem(player, stack, hand); // TODO check if relevant
            player.setStackInHand(hand, ItemStack.EMPTY);
        } else if (stack.getCount() < oldStack.getCount() && player.getAbilities().creativeMode) {
            stack.setCount(oldStack.getCount());
        }
    }
}
