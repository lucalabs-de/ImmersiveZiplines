package de.lucalabs.ziplines.registry;

import de.lucalabs.ziplines.ImmersiveZiplines;
import de.lucalabs.ziplines.blocks.entity.FastenerBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public final class ZiplineBlockEntities {

    public static final BlockEntityType<FastenerBlockEntity> FASTENER = register(
            "fastener",
            () -> BlockEntityType.Builder.create(FastenerBlockEntity::new, ZiplineBlocks.FASTENER).build(null));

    private ZiplineBlockEntities() {}

    private static <T extends BlockEntity> BlockEntityType<T> register(final String name, Supplier<BlockEntityType<T>> supplier) {
        Identifier identifier = Identifier.of(ImmersiveZiplines.MOD_ID, name);
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, identifier, supplier.get());
    }

    public static void initialize() {
        ImmersiveZiplines.LOGGER.info("initializing block entities");
    }
}
