package de.lucalabs.ziplines.fastener.accessor;

import de.lucalabs.ziplines.entity.FenceFastenerEntity;
import de.lucalabs.ziplines.fastener.EntityFastener;
import de.lucalabs.ziplines.fastener.FastenerType;

public final class FenceFastenerAccessor extends EntityFastenerAccessor<FenceFastenerEntity> {
    public FenceFastenerAccessor() {
        super(FenceFastenerEntity.class);
    }

    public FenceFastenerAccessor(final EntityFastener<FenceFastenerEntity> fastener) {
        super(FenceFastenerEntity.class, fastener);
    }

    @Override
    public FastenerType getType() {
        return FastenerType.FENCE;
    }
}
