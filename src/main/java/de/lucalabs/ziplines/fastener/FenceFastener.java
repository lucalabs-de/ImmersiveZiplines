package de.lucalabs.ziplines.fastener;

import de.lucalabs.ziplines.entity.FenceFastenerEntity;
import de.lucalabs.ziplines.fastener.accessor.EntityFastenerAccessor;
import de.lucalabs.ziplines.fastener.accessor.FenceFastenerAccessor;
import net.minecraft.util.math.BlockPos;

public final class FenceFastener extends EntityFastener<FenceFastenerEntity> {
    public FenceFastener(final FenceFastenerEntity entity) {
        super(entity);
    }

    @Override
    public EntityFastenerAccessor<FenceFastenerEntity> createAccessor() {
        return new FenceFastenerAccessor(this);
    }

    @Override
    public BlockPos getPos() {
        return this.entity.getDecorationBlockPos();
    }

    @Override
    public boolean isMoving() {
        return false;
    }
}
