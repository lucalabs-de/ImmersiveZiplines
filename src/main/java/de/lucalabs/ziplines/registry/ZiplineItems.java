package de.lucalabs.ziplines.registry;

import de.lucalabs.ziplines.ImmersiveZiplines;
import de.lucalabs.ziplines.items.Zipline;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public final class ZiplineItems {
    public static final Zipline ZIPLINE = register("zipline", () -> new Zipline(new Item.Settings()));

    private ZiplineItems() {}

    private static <T extends Item> T register(final String name, Supplier<T> supplier) {
        Identifier identifier = Identifier.of(ImmersiveZiplines.MOD_ID, name);
        return Registry.register(Registries.ITEM, identifier, supplier.get());
    }

    public static void initialize() {
        ImmersiveZiplines.LOGGER.info("initializing items");
    }
}
