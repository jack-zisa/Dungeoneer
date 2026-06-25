package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;
import dev.creoii.dungeoneer.util.Identifiable;

public record StraightBulletPathType(String id) implements BulletPathType<StraightBulletPathType.StraightBulletPathInstance> {
    public static final MapCodec<StraightBulletPathType> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> BulletPathType.addDefaultFields(instance).apply(instance, StraightBulletPathType::new));

    @Override
    public Type type() {
        return Type.STRAIGHT;
    }

    @Override
    public StraightBulletPathInstance create() {
        return new StraightBulletPathInstance(this);
    }

    @Override
    public Identifiable withId(String id) {
        return new StraightBulletPathType(id);
    }

    public static class StraightBulletPathInstance extends Instance<StraightBulletPathType> {
        public StraightBulletPathInstance(StraightBulletPathType definition) {
            super(definition);
        }

        @Override
        public float[] getOffset(BulletNode<?> node, float t) {
            return new float[]{0f, t};
        }
    }
}
