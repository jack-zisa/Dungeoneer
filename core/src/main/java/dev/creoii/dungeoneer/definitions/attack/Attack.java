package dev.creoii.dungeoneer.definitions.attack;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.util.Identifiable;

import java.util.function.Function;

/**
 * An attack defines the initial state of one or more {@link dev.creoii.dungeoneer.definitions.sided.BulletNode}.
 */
public sealed interface Attack extends Identifiable permits BulletAttack, CompositeAttack, LaserAttack, ReferenceAttack {
    Type type();

    Codec<Attack> CODEC = Type.CODEC.dispatch(Attack::type, type -> switch (type) {
        case BULLET -> BulletAttack.TYPE_CODEC;
        case LASER -> LaserAttack.TYPE_CODEC;
        case COMPOSITE -> CompositeAttack.TYPE_CODEC;
        case REFERENCE -> ReferenceAttack.TYPE_CODEC;
    });
    Codec<Attack> EITHER_CODEC = Codec.either(Codec.STRING, CODEC).xmap(either -> {
        return either.map(DataManager::getAttack, Function.identity());
    }, Either::right);

    enum Type {
        BULLET,
        LASER,
        COMPOSITE,
        REFERENCE;

        public static final Codec<Type> CODEC = Codec.STRING.xmap(s -> Type.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    }
}
