package de.lucalabs.ziplines.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import de.lucalabs.ziplines.events.ClientEventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;getModelViewStack()Lnet/minecraft/client/util/math/MatrixStack;"
            ))
    private void onDrawEntityHighlight(
            CallbackInfo ci,
            @Local(argsOnly = true) Camera camera,
            @Local(argsOnly = true) float tickDelta,
            @Local(argsOnly = true) MatrixStack matrix,
            @Local VertexConsumerProvider.Immediate buf) {
        HitResult target = MinecraftClient.getInstance().crosshairTarget;
        if (target != null && target.getType() == HitResult.Type.ENTITY) {
            if (target instanceof EntityHitResult entityTarget) {
                ClientEventHandler.onDrawEntityHighlight(entityTarget.getEntity(), camera, tickDelta, matrix, buf);
            }
        }
    }
}
