package de.lucalabs.ziplines.components;

import de.lucalabs.ziplines.ImmersiveZiplines;
import de.lucalabs.ziplines.blocks.entity.FastenerBlockEntity;
import de.lucalabs.ziplines.entity.FenceFastenerEntity;
import de.lucalabs.ziplines.fastener.BlockFastener;
import de.lucalabs.ziplines.fastener.FenceFastener;
import de.lucalabs.ziplines.fastener.PlayerFastener;
import de.lucalabs.ziplines.fastener.RegularBlockView;
import dev.onyxstudios.cca.api.v3.block.BlockComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.block.BlockComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class ZiplineComponents implements EntityComponentInitializer, BlockComponentInitializer {

    public static final Identifier FASTENER_ID = Identifier.of(ImmersiveZiplines.MOD_ID, "fastener");
    public static final ComponentKey<FastenerComponent> FASTENER =
            ComponentRegistry.getOrCreate(FASTENER_ID, FastenerComponent.class);


    @Override
    public void registerBlockComponentFactories(BlockComponentFactoryRegistry registry) {
        registry.registerFor(
                FastenerBlockEntity.class,
                FASTENER,
                be -> new FastenerComponent().setFastener(new BlockFastener(be, new RegularBlockView())));
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(PlayerEntity.class, FASTENER, e -> new FastenerComponent().setFastener(new PlayerFastener(e)));
        registry.registerFor(FenceFastenerEntity.class, FASTENER, e -> new FastenerComponent().setFastener(new FenceFastener(e)));
    }
}
