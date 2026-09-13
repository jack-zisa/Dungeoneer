package dev.creoii.dungeoneer.definitions.attack.bullet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.attack.bullet.path.*;
import dev.creoii.dungeoneer.util.Identifiable;

public record SingleBulletType(String id, Type type, float scale, float angleOffset, float speed, float minSpeed, float maxSpeed, float lifetime, float acceleration, float rotationSpeed, boolean faceDirection, BulletPathType<?> path) implements BulletType {
    public static final MapCodec<SingleBulletType> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Identifiable.idField(),
        Type.CODEC.fieldOf("type").orElse(Type.SINGLE).forGetter(SingleBulletType::type),
        Codec.FLOAT.fieldOf("scale").orElse(1f).forGetter(SingleBulletType::scale),
        Codec.FLOAT.fieldOf("angle_offset").orElse(0f).forGetter(SingleBulletType::angleOffset),
        BulletType.speedField(),
        BulletType.minSpeedField(),
        BulletType.maxSpeedField(),
        BulletType.lifetimeField(),
        BulletType.accelerationField(),
        Codec.FLOAT.fieldOf("rotation_speed").orElse(0f).forGetter(SingleBulletType::rotationSpeed),
        Codec.BOOL.fieldOf("face_direction").orElse(false).forGetter(SingleBulletType::faceDirection),
        BulletType.pathField()
    ).apply(instance, SingleBulletType::new));

    @Override
    public Identifiable withId(String id) {
        return new SingleBulletType(id, type, scale, angleOffset, speed, minSpeed, maxSpeed, lifetime, acceleration, rotationSpeed, faceDirection, path);
    }
}
