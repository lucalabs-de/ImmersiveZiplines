package de.lucalabs.ziplines.registry;

import de.lucalabs.ziplines.ImmersiveZiplines;
import de.lucalabs.ziplines.blocks.FastenerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public final class ZiplineBlocks {

    public static final FastenerBlock FASTENER = register("fastener", () ->
            new FastenerBlock(
                    AbstractBlock.Settings.create()
                            .mapColor(MapColor.IRON_GRAY)
                            .solid()
                            .strength(3.5F)
                            .sounds(BlockSoundGroup.LANTERN)));

    private ZiplineBlocks() {
    }


    private static <T extends Block> T register(final String name, Supplier<T> supplier) {
        Identifier identifier = Identifier.of(ImmersiveZiplines.MOD_ID, name);
        return Registry.register(Registries.BLOCK, identifier, supplier.get());
    }

    public static void initialize() {
        ImmersiveZiplines.LOGGER.info("Registering blocks");
    }
}
