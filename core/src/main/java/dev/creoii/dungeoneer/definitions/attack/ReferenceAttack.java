package dev.creoii.dungeoneer.definitions.attack;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ReferenceAttack(String id, AttackType type) implements Attack {
    public static final MapCodec<ReferenceAttack> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return Attack.addDefaultFields(instance).apply(instance, ReferenceAttack::new);
    });
}
