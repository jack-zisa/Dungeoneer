package dev.creoii.dungeoneer.util.action;

import com.mojang.serialization.Codec;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.util.Identifiable;

public interface Action extends Identifiable {
    Codec<Action> CODEC = Type.CODEC.dispatch(Action::getType, type -> switch (type) {
        case EMPTY -> EmptyAction.CODEC;
        case DAMAGE -> DamageAction.CODEC;
    });

    Type getType();

    void apply(Raid<?, ?, ?, ?> raid, Context context);

    enum Type {
        EMPTY,
        DAMAGE;

        public static final Codec<Type> CODEC = Codec.STRING.xmap(s -> Type.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    }
}
