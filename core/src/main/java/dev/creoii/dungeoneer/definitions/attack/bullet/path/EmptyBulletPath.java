package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.SidedBullet;

public record EmptyBulletPath(String id) implements BulletPath {
    public static final MapCodec<EmptyBulletPath> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return BulletPath.addDefaultFields(instance).apply(instance, EmptyBulletPath::new);
    });

    @Override
    public BulletPathType type() {
        return BulletPathType.EMPTY;
    }

    @Override
    public void apply(SidedBullet bullet, float dt) {
    }
}
