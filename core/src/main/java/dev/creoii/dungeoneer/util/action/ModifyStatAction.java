package dev.creoii.dungeoneer.util.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.util.Context;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.action.value.ValueType;
import dev.creoii.dungeoneer.util.stat.ModifierEntry;

public record ModifyStatAction(String id, ModifierEntry modifier) implements Action {
    public static final MapCodec<ModifyStatAction> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(ModifyStatAction::id),
            ModifierEntry.CODEC.fieldOf("modifier").forGetter(ModifyStatAction::modifier)
        ).apply(instance, ModifyStatAction::new)
    );

    @Override
    public void apply(Raid<?, ?, ?, ?> raid, Context context) {
        if (context.has(ValueType.CHARACTER)) {
            Character<?> character = context.get(ValueType.CHARACTER);
            character.getStats().applyModifier(modifier);
        }
    }

    @Override
    public Type getType() {
        return Type.MODIFY_STAT;
    }

    @Override
    public Identifiable withId(String id) {
        return new ModifyStatAction(id, modifier);
    }
}
