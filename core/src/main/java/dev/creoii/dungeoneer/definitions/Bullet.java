package dev.creoii.dungeoneer.definitions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.util.Identifiable;

public record Bullet(String id, float angleOffset,
                     float speed, float lifetime, float rotationSpeed, float acceleration,
                     float amplitude, float frequency,
                     float orbitSpeed, float orbitRadius
) implements Identifiable {
    public static final Codec<Bullet> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.STRING.fieldOf("id").forGetter(Bullet::id),
            Codec.FLOAT.fieldOf("angle_offset").orElse(0f).forGetter(Bullet::angleOffset),
            Codec.FLOAT.fieldOf("speed").forGetter(Bullet::speed),
            Codec.FLOAT.fieldOf("lifetime").forGetter(Bullet::lifetime),
            Codec.FLOAT.fieldOf("rotation_speed").orElse(0f).forGetter(Bullet::rotationSpeed),
            Codec.FLOAT.fieldOf("acceleration").orElse(0f).forGetter(Bullet::acceleration),
            Codec.FLOAT.fieldOf("amplitude").orElse(0f).forGetter(Bullet::amplitude),
            Codec.FLOAT.fieldOf("frequency").orElse(0f).forGetter(Bullet::frequency),
            Codec.FLOAT.fieldOf("orbit_speed").orElse(0f).forGetter(Bullet::orbitSpeed),
            Codec.FLOAT.fieldOf("orbit_radius").orElse(0f).forGetter(Bullet::orbitRadius)
        ).apply(instance, Bullet::new);
    });
    public static final Codec<Bullet> ID_CODEC = Codec.STRING.xmap(DataManager::getBullet, Bullet::id);
}
