package dev.creoii.dungeoneer.definitions.statuseffect;

import com.badlogic.gdx.graphics.Color;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.util.Codecs;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.action.Action;
import dev.creoii.dungeoneer.util.action.EmptyAction;

public record StatusEffect(String id, Color color, Action applier, Action ticker, Action remover) implements Identifiable {
    public static final Codec<StatusEffect> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(StatusEffect::id),
            Codecs.COLOR.optionalFieldOf("color", Color.BLACK).forGetter(StatusEffect::color),
            Action.CODEC.optionalFieldOf("applier", EmptyAction.INSTANCE).forGetter(StatusEffect::applier),
            Action.CODEC.optionalFieldOf("ticker", EmptyAction.INSTANCE).forGetter(StatusEffect::ticker),
            Action.CODEC.optionalFieldOf("remover", EmptyAction.INSTANCE).forGetter(StatusEffect::remover)
        ).apply(instance, StatusEffect::new);
    });

    @Override
    public Identifiable withId(String id) {
        return new StatusEffect(id, color, applier, ticker, remover);
    }
}
