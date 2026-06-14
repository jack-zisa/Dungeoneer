package dev.creoii.dungeoneer.definitions.attack.bullet;

import com.badlogic.gdx.math.Vector2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.attack.bullet.path.BulletPathType;
import dev.creoii.dungeoneer.util.Codecs;

import java.util.List;

public record GroupBulletType(String id, Type type, float speed, float lifetime, float acceleration, BulletPathType<?> path, List<Child> children) implements BulletType {
    public static final MapCodec<GroupBulletType> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
            Codec.STRING.fieldOf("id").forGetter(GroupBulletType::id),
            Type.CODEC.fieldOf("type").orElse(Type.SINGLE).forGetter(GroupBulletType::type),
            Codec.FLOAT.fieldOf("speed").forGetter(GroupBulletType::speed),
            Codec.FLOAT.fieldOf("lifetime").forGetter(GroupBulletType::lifetime),
            Codec.FLOAT.fieldOf("acceleration").orElse(0f).forGetter(GroupBulletType::acceleration),
            BulletPathType.CODEC.fieldOf("path").orElse(BulletPathType.EMPTY).forGetter(GroupBulletType::path),
            Child.CODEC.listOf().fieldOf("children").forGetter(GroupBulletType::children)
        ).apply(instance, GroupBulletType::new);
    });

    public record Child(Vector2 offset, BulletType definition) {
        public static final Codec<Child> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                Codecs.VECTOR_2.fieldOf("offset").orElse(new Vector2()).forGetter(Child::offset),
                BulletType.CODEC.fieldOf("definition").forGetter(Child::definition)
            ).apply(instance, Child::new);
        });
    }
}
