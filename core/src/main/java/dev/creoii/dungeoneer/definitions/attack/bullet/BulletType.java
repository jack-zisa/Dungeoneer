package dev.creoii.dungeoneer.definitions.attack.bullet;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.attack.bullet.path.BulletPathType;
import dev.creoii.dungeoneer.util.Identifiable;

public interface BulletType extends Identifiable<String> {
    Codec<BulletType> CODEC = Type.CODEC.dispatch(BulletType::type, type -> switch (type) {
        case SINGLE -> SingleBulletType.TYPE_CODEC;
        case GROUP -> GroupBulletType.TYPE_CODEC;
    });

    Type type();

    float speed();

    float lifetime();

    float acceleration();

    BulletPathType<?> path();

    static <T extends BulletType> Products.P2<RecordCodecBuilder.Mu<T>, String, Type> addDefaultFields(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(Codec.STRING.fieldOf("id").forGetter(BulletType::id), Type.CODEC.fieldOf("type").orElse(Type.SINGLE).forGetter(BulletType::type));
    }

    enum Type {
        SINGLE,
        GROUP;

        public static final Codec<Type> CODEC = Codec.STRING.xmap(s -> Type.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    }
}
