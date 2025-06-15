package de.lucalabs.ziplines.mixin;

import de.lucalabs.ziplines.curves.Catenary;
import de.lucalabs.ziplines.curves.SegmentView;
import de.lucalabs.ziplines.registry.ZiplineSounds;
import de.lucalabs.ziplines.utils.Constants;
import de.lucalabs.ziplines.utils.ZiplineUser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerZiplineMixin extends LivingEntity implements ZiplineUser {

    @Shadow public abstract void playSound(SoundEvent sound, float volume, float pitch);

    @Unique
    boolean onZipline = false;

    @Unique
    boolean traveled = false;

    @Unique
    double lastVelocity = 0;

    @Unique
    float ziplineT = 0;

    @Unique
    @Nullable
    Catenary currentZipline;

    @Unique
    double currentZiplineLength;

    @Unique
    @Nullable
    Vec3d currentZiplineAnchor;

    protected PlayerZiplineMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean immersiveZiplines$isUsingZipline() {
        return onZipline;
    }

    @Override
    public void immersiveZiplines$startUsingZipline(Catenary c, Vec3d anchor) {
        this.currentZipline = c;
        this.currentZiplineAnchor = anchor;
        this.currentZiplineLength = c.getLength();
        this.onZipline = true;
        this.traveled = false;
        setNoGravity(true);
        setInitialVelocityAndT();
    }

    @Override
    public void immersiveZiplines$stopUsingZipline() {
        this.onZipline = false;
        this.currentZipline = null;
        this.currentZiplineAnchor = null;
        this.lastVelocity = 0;
        setNoGravity(false);
    }

    @Unique
    private void setInitialVelocityAndT() {
        if (currentZipline != null && currentZiplineAnchor != null) {
            Vec3d curAbsZiplinePlayerPos = getPos().offset(Direction.UP, 2);
            Catenary.PosInSegment closestPointOnZipline = currentZipline.snapToCurve(currentZiplineAnchor.relativize(curAbsZiplinePlayerPos));
            SegmentView curPosOnZipline = closestPointOnZipline.segment();
            Vec3d tangent = curPosOnZipline.getPos(1).subtract(curPosOnZipline.getPos(0)).normalize();

            lastVelocity = getVelocity().dotProduct(tangent);
            ziplineT = closestPointOnZipline.t();
        }
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    @Environment(EnvType.CLIENT)
    private void updateZiplineMovement(CallbackInfo ci) {
        if (onZipline && currentZipline != null && currentZiplineAnchor != null) {
            if (traveled && isOnGround()) {
                immersiveZiplines$stopUsingZipline();
                return;
            }

            traveled = true;

            Vec3d tangent = currentZipline.getTangentAtT(ziplineT);
            double scalarAccelerationByZipline = tangent.dotProduct(Constants.DOWN) * GRAVITY;

            double frictionlessAcceleration = scalarAccelerationByZipline + lastVelocity;
            double totalAcceleration = frictionlessAcceleration - Constants.DRAG * frictionlessAcceleration;
            lastVelocity = totalAcceleration;
            ziplineT = (float) MathHelper.clamp(ziplineT + totalAcceleration / currentZiplineLength, 0, 1);

            Vec3d newPosition = currentZiplineAnchor.add(currentZipline.getT(ziplineT)).offset(Direction.DOWN, 2);
            setPosition(newPosition);
        }
    }
}
