package dev.creoii.dungeoneer.definitions.attack.bullet;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.attack.bullet.path.BulletPath;
import dev.creoii.dungeoneer.util.Identifiable;

public interface BulletDefinition extends Identifiable {
    Codec<BulletDefinition> CODEC = BulletDefinitionType.CODEC.dispatch(BulletDefinition::type, type -> switch (type) {
        case SINGLE -> SingleBulletDefinition.TYPE_CODEC;
        case GROUP -> GroupBulletDefinition.TYPE_CODEC;
    });

    BulletDefinitionType type();

    float speed();

    float angleOffset();

    float lifetime();

    float acceleration();

    BulletPath path();

    float scale();

    float rotationSpeed();

    static <T extends BulletDefinition> Products.P2<RecordCodecBuilder.Mu<T>, String, BulletDefinitionType> addDefaultFields(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(Codec.STRING.fieldOf("id").forGetter(BulletDefinition::id), BulletDefinitionType.CODEC.fieldOf("type").orElse(BulletDefinitionType.SINGLE).forGetter(BulletDefinition::type));
    }
}
