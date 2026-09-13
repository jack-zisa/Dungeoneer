package dev.creoii.dungeoneer.definitions.attack.bullet;

import com.badlogic.gdx.math.Vector2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.attack.bullet.path.BulletPathType;
import dev.creoii.dungeoneer.util.Codecs;
import dev.creoii.dungeoneer.util.Identifiable;

import java.util.List;

public record GroupBulletType(String id, Type type, float speed, float minSpeed, float maxSpeed, float lifetime, float acceleration, BulletPathType<?> path, List<Child> children) implements BulletType {
    public static final MapCodec<GroupBulletType> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Identifiable.idField(),
        Type.CODEC.fieldOf("type").orElse(Type.GROUP).forGetter(GroupBulletType::type),
        BulletType.speedField(),
        BulletType.minSpeedField(),
        BulletType.maxSpeedField(),
        BulletType.lifetimeField(),
        BulletType.accelerationField(),
        BulletType.pathField(),
        Child.CODEC.listOf().fieldOf("children").forGetter(GroupBulletType::children)
    ).apply(instance, GroupBulletType::new));

    @Override
    public Identifiable withId(String id) {
        return new GroupBulletType(id, type, speed, minSpeed, maxSpeed, lifetime, acceleration, path, children);
    }

    public record Child(Vector2 offset, BulletType definition) {
        public static final Codec<Child> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.VECTOR_2.fieldOf("offset").orElse(new Vector2()).forGetter(Child::offset),
            BulletType.CODEC.fieldOf("definition").forGetter(Child::definition)
        ).apply(instance, Child::new));
    }
}
