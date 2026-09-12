package dev.creoii.dungeoneer.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public interface Identifiable {
    String id();

    Identifiable withId(String id);

    static <T extends Identifiable> RecordCodecBuilder<T, String> idField() {
        return Codec.STRING.optionalFieldOf("id", "").forGetter(Identifiable::id);
    }
}
