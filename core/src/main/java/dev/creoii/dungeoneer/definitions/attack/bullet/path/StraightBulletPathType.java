package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;

public record StraightBulletPathType(String id) implements BulletPathType<StraightBulletPathType.StraightBulletPathInstance> {
    public static final MapCodec<StraightBulletPathType> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return BulletPathType.addDefaultFields(instance).apply(instance, StraightBulletPathType::new);
    });

    @Override
    public Type type() {
        return Type.STRAIGHT;
    }

    @Override
    public StraightBulletPathInstance create() {
        return new StraightBulletPathInstance(this);
    }

    public static class StraightBulletPathInstance extends Instance<StraightBulletPathType> {
        public StraightBulletPathInstance(StraightBulletPathType definition) {
            super(definition);
        }

        @Override
        public void reset() {
        }

        @Override
        public void update(BulletNode bullet, float dt) {
            bullet.setPos(bullet.getStartX() + bullet.getDirX() * bullet.getDistanceTravelled(), bullet.getStartY() + bullet.getDirY() * bullet.getDistanceTravelled());
        }
    }
}
