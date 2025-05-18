package de.lucalabs.ziplines;

import de.lucalabs.ziplines.net.serverbound.InteractionConnectionMessage;
import de.lucalabs.ziplines.registry.*;
import de.lucalabs.ziplines.tags.Tags;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
	}
}