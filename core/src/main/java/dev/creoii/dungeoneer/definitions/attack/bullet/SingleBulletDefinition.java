package dev.creoii.dungeoneer.definitions.attack.bullet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.attack.bullet.path.*;

public record SingleBulletDefinition(String id, BulletDefinitionType type, float scale, float angleOffset, float speed, float lifetime, float rotationSpeed, float acceleration, BulletPath path) implements BulletDefinition {
    public static final MapCodec<SingleBulletDefinition> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
            Codec.STRING.fieldOf("id").forGetter(SingleBulletDefinition::id),
            BulletDefinitionType.CODEC.fieldOf("type").orElse(BulletDefinitionType.SINGLE).forGetter(SingleBulletDefinition::type),
            Codec.FLOAT.fieldOf("scale").orElse(1f).forGetter(SingleBulletDefinition::scale),
            Codec.FLOAT.fieldOf("angle_offset").orElse(0f).forGetter(SingleBulletDefinition::angleOffset),
            Codec.FLOAT.fieldOf("speed").forGetter(SingleBulletDefinition::speed),
            Codec.FLOAT.fieldOf("lifetime").forGetter(SingleBulletDefinition::lifetime),
            Codec.FLOAT.fieldOf("rotation_speed").orElse(0f).forGetter(SingleBulletDefinition::rotationSpeed),
            Codec.FLOAT.fieldOf("acceleration").orElse(0f).forGetter(SingleBulletDefinition::acceleration),
            BulletPath.CODEC.fieldOf("path").orElse(BulletPath.EMPTY).forGetter(SingleBulletDefinition::path)
        ).apply(instance, SingleBulletDefinition::new);
    });
}
