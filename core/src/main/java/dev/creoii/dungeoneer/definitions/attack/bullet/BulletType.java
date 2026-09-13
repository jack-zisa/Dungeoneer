package dev.creoii.dungeoneer.definitions.attack.bullet;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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

    float minSpeed();

    float maxSpeed();

    float lifetime();

    float acceleration();

    BulletPathType<?> path();

    static <T extends BulletType> RecordCodecBuilder<T, Float> speedField() {
        return Codec.FLOAT.optionalFieldOf("speed", 0f).forGetter(BulletType::speed);
    }

    static <T extends BulletType> RecordCodecBuilder<T, Float> minSpeedField() {
        return Codec.FLOAT.optionalFieldOf("min_speed", Float.MIN_VALUE).forGetter(BulletType::minSpeed);
    }

    static <T extends BulletType> RecordCodecBuilder<T, Float> maxSpeedField() {
        return Codec.FLOAT.optionalFieldOf("max_speed", Float.MAX_VALUE).forGetter(BulletType::maxSpeed);
    }

    static <T extends BulletType> RecordCodecBuilder<T, Float> lifetimeField() {
        return Codec.FLOAT.fieldOf("lifetime").forGetter(BulletType::lifetime);
    }

    static <T extends BulletType> RecordCodecBuilder<T, Float> accelerationField() {
        return Codec.FLOAT.optionalFieldOf("acceleration", 0f).forGetter(BulletType::acceleration);
    }

    static <T extends BulletType> RecordCodecBuilder<T, BulletPathType<?>> pathField() {
        return BulletPathType.CODEC.fieldOf("path").orElse(BulletPathType.EMPTY).forGetter(BulletType::path);
    }

    enum Type {
        SINGLE,
        GROUP;

        public static final Codec<Type> CODEC = Codec.STRING.xmap(s -> Type.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    }
}
