package de.lucalabs.ziplines.renderer;

import de.lucalabs.ziplines.ImmersiveZiplines;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

public class RenderConstants {

    public static final float HIGHLIGHT_ALPHA = 0.4F;

    @SuppressWarnings("deprecation")
    public static final SpriteIdentifier SOLID_TEXTURE = new SpriteIdentifier(
            SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
            new Identifier(ImmersiveZiplines.MOD_ID, "entity/connections"));

    @SuppressWarnings("deprecation")
    public static final SpriteIdentifier TRANSLUCENT_TEXTURE = new SpriteIdentifier(
            SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
            new Identifier(ImmersiveZiplines.MOD_ID, "entity/connections"));

}
