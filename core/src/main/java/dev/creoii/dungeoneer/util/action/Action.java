package dev.creoii.dungeoneer.util.action;

import com.mojang.serialization.Codec;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.util.Context;
import dev.creoii.dungeoneer.util.Identifiable;

public interface Action extends Identifiable {
    Codec<Action> CODEC = Type.CODEC.dispatch(Action::getType, type -> switch (type) {
        case EMPTY -> EmptyAction.CODEC;
        case DAMAGE -> DamageAction.CODEC;
        case HEAL -> HealAction.CODEC;
        case MODIFY_STAT -> ModifyStatAction.CODEC;
        case UNMODIFY_STAT -> UnmodifyStatAction.CODEC;
    });

    Type getType();

    void apply(Raid<?, ?, ?, ?> raid, Context context);

    enum Type {
        EMPTY,
        DAMAGE,
        HEAL,
        MODIFY_STAT,
        UNMODIFY_STAT;

        public static final Codec<Type> CODEC = Codec.STRING.xmap(s -> Type.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    }
}
