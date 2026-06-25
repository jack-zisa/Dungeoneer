package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;
import dev.creoii.dungeoneer.util.Identifiable;

public record EmptyBulletPathType(String id) implements BulletPathType<EmptyBulletPathType.EmptyBulletPathInstance> {
    public static final MapCodec<EmptyBulletPathType> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> BulletPathType.addDefaultFields(instance).apply(instance, EmptyBulletPathType::new));

    @Override
    public Type type() {
        return Type.EMPTY;
    }

    @Override
    public EmptyBulletPathInstance create() {
        return new EmptyBulletPathInstance(this);
    }

    @Override
    public Identifiable withId(String id) {
        return new EmptyBulletPathType(id);
    }

    public static class EmptyBulletPathInstance extends Instance<EmptyBulletPathType> {
        public EmptyBulletPathInstance(EmptyBulletPathType definition) {
            super(definition);
        }
    }
}
