package de.lucalabs.ziplines.registry;

import de.lucalabs.ziplines.ImmersiveZiplines;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ZiplineSounds {
    public static final SoundEvent ZIPLINE_ATTACH = register("zipline_attach");
    public static final SoundEvent ZIPLINE_INTERRUPT = register("zipline_interrupt");
    public static final SoundEvent ZIPLINE_USE = register("zipline_use");
    public static final SoundEvent CORD_STRETCH = register("cord.stretch");
    public static final SoundEvent CORD_CONNECT = register("cord.connect");
    public static final SoundEvent CORD_DISCONNECT = register("cord.disconnect");
    public static final SoundEvent CORD_SNAP = register("cord.snap");

    private ZiplineSounds() {
    }

    private static SoundEvent register(String path) {
        Identifier id = new Identifier(ImmersiveZiplines.MOD_ID, path);

        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void initialize() {
        ImmersiveZiplines.LOGGER.info("initializing sounds");
    }
}
