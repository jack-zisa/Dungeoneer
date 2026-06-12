package dev.creoii.dungeoneer.definitions.attack.bullet;

import com.badlogic.gdx.math.Vector2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.attack.bullet.path.BulletPath;
import dev.creoii.dungeoneer.util.Codecs;

import java.util.List;

public record GroupBulletDefinition(String id, BulletDefinitionType type, float scale, float angleOffset, float speed, float lifetime, float rotationSpeed, float acceleration, BulletPath path, List<Child> children) implements BulletDefinition {
    public static final MapCodec<GroupBulletDefinition> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
            Codec.STRING.fieldOf("id").forGetter(GroupBulletDefinition::id),
            BulletDefinitionType.CODEC.fieldOf("type").orElse(BulletDefinitionType.SINGLE).forGetter(GroupBulletDefinition::type),
            Codec.FLOAT.fieldOf("scale").orElse(1f).forGetter(GroupBulletDefinition::scale),
            Codec.FLOAT.fieldOf("angle_offset").orElse(0f).forGetter(GroupBulletDefinition::angleOffset),
            Codec.FLOAT.fieldOf("speed").forGetter(GroupBulletDefinition::speed),
            Codec.FLOAT.fieldOf("lifetime").forGetter(GroupBulletDefinition::lifetime),
            Codec.FLOAT.fieldOf("rotation_speed").orElse(0f).forGetter(GroupBulletDefinition::rotationSpeed),
            Codec.FLOAT.fieldOf("acceleration").orElse(0f).forGetter(GroupBulletDefinition::acceleration),
            BulletPath.CODEC.fieldOf("path").orElse(BulletPath.EMPTY).forGetter(GroupBulletDefinition::path),
            Child.CODEC.listOf().fieldOf("children").forGetter(GroupBulletDefinition::children)
        ).apply(instance, GroupBulletDefinition::new);
    });

    public record Child(Vector2 offset, BulletDefinition definition) {
        public static final Codec<Child> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                Codecs.VECTOR_2.fieldOf("offset").orElse(new Vector2()).forGetter(Child::offset),
                BulletDefinition.CODEC.fieldOf("definition").forGetter(Child::definition)
            ).apply(instance, Child::new);
        });
    }
}
