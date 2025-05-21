package de.lucalabs.ziplines;

import de.lucalabs.ziplines.utils.ZiplineUser;
import de.lucalabs.ziplines.net.serverbound.InteractionConnectionMessage;
import de.lucalabs.ziplines.registry.*;
import de.lucalabs.ziplines.tags.Tags;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.TypedActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImmersiveZiplines implements ModInitializer {
	public static final String MOD_ID = "immersive-ziplines";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ZiplineBlocks.initialize();
		ZiplineBlockEntities.initialize();
		ZiplineItems.initialize();
		ZiplineSounds.initialize();
		ZiplineEntities.initialize();
		Tags.initialize();

		ServerPlayNetworking.registerGlobalReceiver(InteractionConnectionMessage.ID, InteractionConnectionMessage::apply);

		// TODO also stop ziplining if the player switches the item in their hand
		UseItemCallback.EVENT.register((playerEntity, world, hand) -> {
			if (playerEntity instanceof ZiplineUser ziplineUser) {
				if(ziplineUser.immersiveZiplines$isUsingZipline()) {
					ziplineUser.immersiveZiplines$stopUsingZipline();
				}
			}

			return TypedActionResult.success(playerEntity.getStackInHand(hand));
		});

	}
}