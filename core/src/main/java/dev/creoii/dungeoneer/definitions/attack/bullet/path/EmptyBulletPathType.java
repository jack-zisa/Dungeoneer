package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;

public record EmptyBulletPathType(String id) implements BulletPathType<EmptyBulletPathType.EmptyBulletPathInstance> {
    public static final MapCodec<EmptyBulletPathType> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return BulletPathType.addDefaultFields(instance).apply(instance, EmptyBulletPathType::new);
    });

    @Override
    public Type type() {
        return Type.EMPTY;
    }

    @Override
    public EmptyBulletPathInstance create() {
        return new EmptyBulletPathInstance(this);
    }

    public static class EmptyBulletPathInstance extends Instance<EmptyBulletPathType> {
        public EmptyBulletPathInstance(EmptyBulletPathType definition) {
            super(definition);
        }

        @Override
        public void reset() {
        }

        @Override
        public void update(BulletNode bullet, float dt) {
        }
    }
}
