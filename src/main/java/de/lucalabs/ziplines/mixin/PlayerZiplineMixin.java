package de.lucalabs.ziplines.mixin;

import de.lucalabs.ziplines.curves.Catenary;
import de.lucalabs.ziplines.curves.SegmentView;
import de.lucalabs.ziplines.utils.Constants;
import de.lucalabs.ziplines.utils.ZiplineUser;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerZiplineMixin extends LivingEntity implements ZiplineUser {
    @Unique
    boolean onZipline = false;

    @Unique
    @Nullable
    Catenary currentZipline;

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
        this.onZipline = true;
        setNoGravity(true);
    }

    @Override
    public void immersiveZiplines$stopUsingZipline() {
        this.onZipline = false;
        this.currentZipline = null;
        this.currentZiplineAnchor = null;
        setNoGravity(false);
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void updateZiplineMovement(CallbackInfo ci) {
        if (onZipline && currentZipline != null && currentZiplineAnchor != null) {
            if (isOnGround()) {
                immersiveZiplines$stopUsingZipline();
                return;
            }

            Vec3d curAbsZiplinePlayerPos = getPos().offset(Direction.UP, 2);

            Catenary.PosInSegment ziplineReference = currentZipline.snapToCurve(currentZiplineAnchor.relativize(curAbsZiplinePlayerPos));

            SegmentView curPosOnZipline = ziplineReference.segment();
            Vec3d tangent = curPosOnZipline.getPos(1).subtract(curPosOnZipline.getPos(0)).normalize();
            double velocityInZiplineDirection = getVelocity().dotProduct(tangent);
            double scalarAccelerationByZipline = tangent.dotProduct(Constants.GRAVITY_ACCELERATION);
            // noinspection UnnecessaryLocalVariable
            Vec3d accelerationByZipline = tangent.multiply(scalarAccelerationByZipline);

            // noinspection UnnecessaryLocalVariable
            Vec3d velocityFromZipline = accelerationByZipline /* × 1 tick */;
            Vec3d previousPreservedVelocity = tangent.multiply(velocityInZiplineDirection);
            Vec3d totalVelocity = velocityFromZipline.add(previousPreservedVelocity);



            setVelocity(totalVelocity);
            velocityModified = true;
        }
    }
}
