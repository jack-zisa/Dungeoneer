package dev.creoii.dungeoneer.util.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.util.Context;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.action.value.ValueType;

public record HealAction(String id, int amount) implements Action {
    public static final MapCodec<HealAction> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(HealAction::id),
            Codec.INT.fieldOf("amount").forGetter(HealAction::amount)
        ).apply(instance, HealAction::new)
    );

    @Override
    public void apply(Raid<?, ?, ?, ?> raid, Context context) {
        if (context.has(ValueType.CHARACTER)) {
            Character<?> character = context.get(ValueType.CHARACTER);
            character.heal(amount);
        }
    }

    @Override
    public Type getType() {
        return Type.HEAL;
    }

    @Override
    public Identifiable withId(String id) {
        return new HealAction(id, amount);
    }
}
