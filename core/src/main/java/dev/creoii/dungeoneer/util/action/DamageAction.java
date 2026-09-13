package dev.creoii.dungeoneer.util.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.util.Context;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.action.value.ValueType;

public record DamageAction(String id, int damage) implements Action {
    public static final MapCodec<DamageAction> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(DamageAction::id),
            Codec.INT.fieldOf("damage").forGetter(DamageAction::damage)
        ).apply(instance, DamageAction::new)
    );

    @Override
    public void apply(Raid<?, ?, ?, ?> raid, Context context) {
        if (context.has(ValueType.CHARACTER)) {
            Character<?> character = context.get(ValueType.CHARACTER);
            character.damage(damage);
        }
    }

    @Override
    public Type getType() {
        return Type.DAMAGE;
    }

    @Override
    public Identifiable withId(String id) {
        return new DamageAction(id, damage);
    }
}
