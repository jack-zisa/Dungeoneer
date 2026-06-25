package dev.creoii.dungeoneer.definitions.attack;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.util.Identifiable;

import java.util.List;

public record CompositeAttack(String id, List<Attack> attacks) implements Attack {
    public static final MapCodec<CompositeAttack> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return Attack.addDefaultFields(instance).and(Attack.EITHER_CODEC.listOf().fieldOf("attacks").forGetter(CompositeAttack::attacks)).apply(instance, CompositeAttack::new);
    });

    @Override
    public AttackType type() {
        return AttackType.COMPOSITE;
    }

    @Override
    public Identifiable withId(String id) {
        return new CompositeAttack(id, attacks);
    }
}
