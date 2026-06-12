package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.SidedBullet;

public record StraightBulletPath(String id, BulletPathType type) implements BulletPath {
    public static final MapCodec<StraightBulletPath> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return BulletPath.addDefaultFields(instance).apply(instance, StraightBulletPath::new);
    });

    @Override
    public void apply(SidedBullet bullet, float dt) {
        bullet.getPos().set(bullet.getStartX() + bullet.getDirection().x * bullet.getDistanceTravelled(), bullet.getStartY() + bullet.getDirection().y * bullet.getDistanceTravelled());
    }
}
