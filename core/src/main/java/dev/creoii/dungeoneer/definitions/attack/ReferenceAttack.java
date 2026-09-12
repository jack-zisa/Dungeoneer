package dev.creoii.dungeoneer.definitions.attack;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.util.Identifiable;

public record ReferenceAttack(String id) implements Attack {
    public static final MapCodec<ReferenceAttack> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
            Identifiable.idField()
        ).apply(instance, ReferenceAttack::new);
    });

    @Override
    public Type type() {
        return Type.REFERENCE;
    }

    @Override
    public Identifiable withId(String id) {
        return new ReferenceAttack(this.id);
    }
}
