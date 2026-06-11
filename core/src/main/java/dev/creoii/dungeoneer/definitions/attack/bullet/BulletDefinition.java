package dev.creoii.dungeoneer.definitions.attack.bullet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.attack.bullet.path.BulletPath;
import dev.creoii.dungeoneer.util.Identifiable;

public record BulletDefinition(String id, float scale, float angleOffset, float speed, float lifetime, float rotationSpeed, float acceleration, BulletPath path) implements Identifiable {
    public static final Codec<BulletDefinition> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.STRING.fieldOf("id").forGetter(BulletDefinition::id),
            Codec.FLOAT.fieldOf("scale").orElse(1f).forGetter(BulletDefinition::scale),
            Codec.FLOAT.fieldOf("angle_offset").orElse(0f).forGetter(BulletDefinition::angleOffset),
            Codec.FLOAT.fieldOf("speed").forGetter(BulletDefinition::speed),
            Codec.FLOAT.fieldOf("lifetime").forGetter(BulletDefinition::lifetime),
            Codec.FLOAT.fieldOf("rotation_speed").orElse(0f).forGetter(BulletDefinition::rotationSpeed),
            Codec.FLOAT.fieldOf("acceleration").orElse(0f).forGetter(BulletDefinition::acceleration),
            BulletPath.CODEC.fieldOf("path").forGetter(BulletDefinition::path)
        ).apply(instance, BulletDefinition::new);
    });
    public static final Codec<BulletDefinition> ID_CODEC = Codec.STRING.xmap(DataManager::getBullet, BulletDefinition::id);
}
