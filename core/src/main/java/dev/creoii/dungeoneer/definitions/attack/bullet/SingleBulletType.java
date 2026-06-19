package dev.creoii.dungeoneer.definitions.attack.bullet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.attack.bullet.path.*;

public record SingleBulletType(String id, Type type, float scale, float angleOffset, float speed, float lifetime, float rotationSpeed, float acceleration, BulletPathType<?> path) implements BulletType {
    public static final MapCodec<SingleBulletType> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("id").forGetter(SingleBulletType::id),
        Type.CODEC.fieldOf("type").orElse(Type.SINGLE).forGetter(SingleBulletType::type),
        Codec.FLOAT.fieldOf("scale").orElse(1f).forGetter(SingleBulletType::scale),
        Codec.FLOAT.fieldOf("angle_offset").orElse(0f).forGetter(SingleBulletType::angleOffset),
        Codec.FLOAT.fieldOf("speed").forGetter(SingleBulletType::speed),
        Codec.FLOAT.fieldOf("lifetime").forGetter(SingleBulletType::lifetime),
        Codec.FLOAT.fieldOf("rotation_speed").orElse(0f).forGetter(SingleBulletType::rotationSpeed),
        Codec.FLOAT.fieldOf("acceleration").orElse(0f).forGetter(SingleBulletType::acceleration),
        BulletPathType.CODEC.fieldOf("path").orElse(BulletPathType.EMPTY).forGetter(SingleBulletType::path)
    ).apply(instance, SingleBulletType::new));
}
