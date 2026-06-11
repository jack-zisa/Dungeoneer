package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.SidedBullet;

public record ForwardBulletPath(String id, BulletPathType type) implements BulletPath {
    public static final MapCodec<ForwardBulletPath> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return BulletPath.addDefaultFields(instance).apply(instance, ForwardBulletPath::new);
    });

    @Override
    public void apply(SidedBullet bullet, float dt) {
        bullet.incrementSpeed(bullet.getDefinition().acceleration() * dt);
        bullet.incrementDistanceTravelled(bullet.getSpeed() * dt);
        bullet.getPos().set(bullet.getStartX() + bullet.getDirection().x * bullet.getDistanceTravelled(), bullet.getStartY() + bullet.getDirection().y * bullet.getDistanceTravelled());
    }
}
