package dev.creoii.dungeoneer.definitions.attack;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.util.Identifiable;

public record ReferenceAttack(String id) implements Attack {
    public static final MapCodec<ReferenceAttack> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return Attack.addDefaultFields(instance).apply(instance, ReferenceAttack::new);
    });

    @Override
    public AttackType type() {
        return AttackType.REFERENCE;
    }

    @Override
    public Identifiable<String> withId(String id) {
        return new ReferenceAttack(this.id);
    }
}
