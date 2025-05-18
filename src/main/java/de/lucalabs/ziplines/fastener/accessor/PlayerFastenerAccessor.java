package de.lucalabs.ziplines.fastener.accessor;

import de.lucalabs.ziplines.fastener.FastenerType;
import de.lucalabs.ziplines.fastener.PlayerFastener;
import net.minecraft.entity.player.PlayerEntity;

public final class PlayerFastenerAccessor extends EntityFastenerAccessor<PlayerEntity> {
    public PlayerFastenerAccessor() {
        super(PlayerEntity.class);
    }

    public PlayerFastenerAccessor(final PlayerFastener fastener) {
        super(PlayerEntity.class, fastener);
    }

    @Override
    public FastenerType getType() {
        return FastenerType.PLAYER;
    }
}
