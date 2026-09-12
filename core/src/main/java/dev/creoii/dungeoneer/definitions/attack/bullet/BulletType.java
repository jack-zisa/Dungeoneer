package dev.creoii.dungeoneer.definitions.attack.bullet;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.attack.bullet.path.BulletPathType;
import dev.creoii.dungeoneer.util.Identifiable;

import java.util.function.Function;

public interface BulletType extends Identifiable {
    Codec<BulletType> CODEC = Type.CODEC.dispatch(BulletType::type, type -> switch (type) {
        case SINGLE -> SingleBulletType.TYPE_CODEC;
        case GROUP -> GroupBulletType.TYPE_CODEC;
    });
    Codec<BulletType> EITHER_CODEC = Codec.either(Codec.STRING, CODEC).xmap(either -> {
        return either.map(DataManager::getBullet, Function.identity());
    }, Either::right);

    Type type();

    float speed();

    float lifetime();

    float acceleration();

    BulletPathType<?> path();

    enum Type {
        SINGLE,
        GROUP;

        public static final Codec<Type> CODEC = Codec.STRING.xmap(s -> Type.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    }
}
