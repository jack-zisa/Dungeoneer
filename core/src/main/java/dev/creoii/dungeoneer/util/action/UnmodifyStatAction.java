package dev.creoii.dungeoneer.util.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.util.Codecs;
import dev.creoii.dungeoneer.util.context.Context;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.action.value.ValueType;
import dev.creoii.dungeoneer.util.stat.Stat;

import java.util.UUID;

public record UnmodifyStatAction(String id, Stat.Type statType, UUID uuid) implements Action {
    public static final MapCodec<UnmodifyStatAction> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(UnmodifyStatAction::id),
            Stat.Type.CODEC.fieldOf("stat_type").forGetter(UnmodifyStatAction::statType),
            Codecs.UUID.fieldOf("uuid").forGetter(UnmodifyStatAction::uuid)
        ).apply(instance, UnmodifyStatAction::new)
    );

    @Override
    public void apply(Raid<?, ?, ?, ?> raid, Context context) {
        if (context.has(ValueType.ENTITY)) {
            Character<?> character = context.get(ValueType.ENTITY);
            character.getStats().removeModifier(statType, uuid);
        }
    }

    @Override
    public Type getType() {
        return Type.UNMODIFY_STAT;
    }

    @Override
    public Identifiable withId(String id) {
        return new UnmodifyStatAction(id, statType, uuid);
    }
}
