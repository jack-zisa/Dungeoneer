package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.mojang.serialization.MapCodec;
import dev.creoii.dungeoneer.util.Identifiable;

public record EmptyBulletPathType() implements BulletPathType<EmptyBulletPathType.EmptyBulletPathInstance> {
    public static final EmptyBulletPathType TYPE_INSTANCE = new EmptyBulletPathType();
    private static final EmptyBulletPathInstance INSTANCE = new EmptyBulletPathInstance(TYPE_INSTANCE);
    public static final MapCodec<EmptyBulletPathType> TYPE_CODEC = MapCodec.unit(TYPE_INSTANCE);

    @Override
    public String id() {
        return "empty";
    }

    @Override
    public Type type() {
        return Type.EMPTY;
    }

    @Override
    public EmptyBulletPathInstance create() {
        return INSTANCE;
    }

    @Override
    public Identifiable withId(String id) {
        return TYPE_INSTANCE;
    }

    public static class EmptyBulletPathInstance extends Instance<EmptyBulletPathType> {
        public EmptyBulletPathInstance(EmptyBulletPathType definition) {
            super(definition);
        }
    }
}
