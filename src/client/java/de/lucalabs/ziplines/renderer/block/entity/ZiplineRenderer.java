package de.lucalabs.ziplines.renderer.block.entity;

import de.lucalabs.ziplines.ImmersiveZiplines;
import de.lucalabs.ziplines.connection.Connection;
import de.lucalabs.ziplines.curves.Catenary;
import de.lucalabs.ziplines.curves.SegmentIterator;
import de.lucalabs.ziplines.utils.MathHelper;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import java.util.function.Function;

public class ZiplineRenderer {
    private static final Identifier MODEL_TEXTURE = new Identifier(ImmersiveZiplines.MOD_ID, "textures/block/rope.png");
    private static final Identifier MODEL_TEXTURE_ALT = new Identifier(ImmersiveZiplines.MOD_ID, "textures/block/rope_alt.png");

    private final WireModel model;
    private final float wireInflate;

    protected ZiplineRenderer(final Function<EntityModelLayer, ModelPart> baker, final EntityModelLayer wireModelLocation) {
        this(baker, wireModelLocation, 0.0F);
    }

    protected ZiplineRenderer(final Function<EntityModelLayer, ModelPart> baker, final EntityModelLayer wireModelLocation, final float wireInflate) {
        this.model = new WireModel(baker.apply(wireModelLocation));
        this.wireInflate = wireInflate;
    }

    public static TexturedModelData wireLayer() {
        return WireModel.createLayer(8, 8, 2);
    }

    public void render(
            final Connection conn,
            final float delta,
            final MatrixStack matrix,
            final VertexConsumerProvider source,
            final int packedLight,
            final int packedOverlay) {

        final Catenary currCat = conn.getCatenary();
        final Catenary prevCat = conn.getPrevCatenary();

        if (currCat != null && prevCat != null) {
            final Catenary cat = prevCat.lerp(currCat, delta);
            final SegmentIterator it = cat.iterator();
            VertexConsumer texturedBuf = source.getBuffer(RenderLayer.getEntityCutoutNoCull(MODEL_TEXTURE));
            VertexConsumer texturedBufAlt = source.getBuffer(RenderLayer.getEntityCutoutNoCull(MODEL_TEXTURE_ALT));
            VertexConsumer[] bufs = {texturedBuf, texturedBufAlt};

            while (it.next()) {
                matrix.push();
                matrix.translate(it.getX(0.0F), it.getY(0.0F), it.getZ(0.0F));
                matrix.multiply(RotationAxis.POSITIVE_Y.rotation(MathHelper.PI / 2.0F - it.getYaw()));
                matrix.multiply(RotationAxis.POSITIVE_X.rotation(-it.getPitch()));
                matrix.scale(1.0F + this.wireInflate, 1.0F, it.getLength() * 16.0F);
                this.model.render(matrix, bufs[conn.getWorld().getRandom().nextInt(1)], packedLight, packedOverlay, 1, 1, 1, 1.0F);
                matrix.pop();
            }
        }
    }

    public static class WireModel extends Model {
        final ModelPart root;

        WireModel(final ModelPart root) {
            super(RenderLayer::getEntityCutout);
            this.root = root;
        }

        public static TexturedModelData createLayer(final int u, final int v, final int size) {
            ModelData mesh = new ModelData();
            mesh.getRoot().addChild("root", ModelPartBuilder.create()
                    .uv(u, v)
                    .cuboid(-size * 0.5F, -size * 0.5F, 0.0F, size, size, 1.0F), ModelTransform.NONE);
            return TexturedModelData.of(mesh, 4, 4);
        }

        @Override
        public void render(final MatrixStack matrix, final VertexConsumer builder, final int light, final int overlay, final float r, final float g, final float b, final float a) {
            this.root.render(matrix, builder, light, overlay, r, g, b, a);
        }
    }
}
