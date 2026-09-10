package dev.creoii.dungeoneer.definitions.statuseffect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.util.Identifiable;

public record StatusEffect(String id) implements Identifiable {
    public static final Codec<StatusEffect> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(StatusEffect::id)
        ).apply(instance, StatusEffect::new);
    });

    @Override
    public String id() {
        return id;
    }

    @Override
    public Identifiable withId(String id) {
        return new StatusEffect(id);
    }
}
