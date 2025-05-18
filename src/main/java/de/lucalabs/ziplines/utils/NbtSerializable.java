package de.lucalabs.ziplines.utils;

import net.minecraft.nbt.NbtCompound;

public interface NbtSerializable {
    NbtCompound serialize();

    void deserialize(NbtCompound compound);
}
