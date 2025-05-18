package de.lucalabs.ziplines;

import de.lucalabs.ziplines.fastener.RegularBlockView;
import de.lucalabs.ziplines.model.BowModel;
import de.lucalabs.ziplines.registry.ZiplineBlockEntities;
import de.lucalabs.ziplines.registry.ZiplineEntities;
import de.lucalabs.ziplines.renderer.ModelLayers;
import de.lucalabs.ziplines.renderer.block.entity.FastenerBlockEntityRenderer;
import de.lucalabs.ziplines.renderer.block.entity.ZiplineRenderer;
import de.lucalabs.ziplines.renderer.entity.FenceFastenerRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class ImmersiveZiplinesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRendererFactories.register(
				ZiplineBlockEntities.FASTENER,
				context -> new FastenerBlockEntityRenderer(context, new RegularBlockView()));

		EntityRendererRegistry.register(ZiplineEntities.FASTENER, FenceFastenerRenderer::new);

		EntityModelLayerRegistry.registerModelLayer(ModelLayers.BOW, BowModel::createLayer);
		EntityModelLayerRegistry.registerModelLayer(ModelLayers.ZIPLINE_WIRE, ZiplineRenderer::wireLayer);

		ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> {
			// Tell Fabric to load this model during resource reloading
			out.accept(FenceFastenerRenderer.MODEL);
		});
	}
}