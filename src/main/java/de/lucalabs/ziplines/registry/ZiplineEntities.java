package de.lucalabs.ziplines.registry;

import de.lucalabs.ziplines.ImmersiveZiplines;
import de.lucalabs.ziplines.entity.FenceFastenerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public final class ZiplineEntities {

    public static final EntityType<FenceFastenerEntity> FASTENER = register("fastener", () ->
            EntityType.Builder.<FenceFastenerEntity>create(FenceFastenerEntity::new, SpawnGroup.MISC)
                    .setDimensions(1.15F, 2.8F)
                    .maxTrackingRange(10)
                    .trackingTickInterval(Integer.MAX_VALUE)
                    .build(ImmersiveZiplines.MOD_ID + ":fastener")
    );

    private ZiplineEntities() {
    }

    private static <T extends Entity> EntityType<T> register(final String name, Supplier<EntityType<T>> supplier) {
        Identifier identifier = Identifier.of(ImmersiveZiplines.MOD_ID, name);
        return Registry.register(Registries.ENTITY_TYPE, identifier, supplier.get());
    }

    public static void initialize() {
        ImmersiveZiplines.LOGGER.info("Registering entities");
    }
}
