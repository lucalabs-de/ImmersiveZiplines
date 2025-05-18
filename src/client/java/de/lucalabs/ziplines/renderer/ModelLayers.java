package de.lucalabs.ziplines.renderer;

import de.lucalabs.ziplines.ImmersiveZiplines;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public final class ModelLayers {
    public static final EntityModelLayer ZIPLINE_WIRE = of("zipline_wire");
    public static final EntityModelLayer BOW = of("bow");

    private ModelLayers() {}

    private static EntityModelLayer of(String name) {
        return layer(name);
    }

    private static EntityModelLayer layer(String name) {
        return new EntityModelLayer(new Identifier(ImmersiveZiplines.MOD_ID, name), "main");
    }
}
