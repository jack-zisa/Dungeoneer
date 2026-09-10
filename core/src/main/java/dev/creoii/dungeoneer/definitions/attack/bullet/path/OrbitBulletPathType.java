package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.badlogic.gdx.math.MathUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;
import dev.creoii.dungeoneer.util.Identifiable;

public record OrbitBulletPathType(String id, int sides, float orbitRadius) implements BulletPathType<OrbitBulletPathType.OrbitBulletPathInstance> {
    public static final MapCodec<OrbitBulletPathType> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> BulletPathType.addDefaultFields(instance).and(instance.group(
        Codec.INT.fieldOf("sides").orElse(-1).forGetter(OrbitBulletPathType::sides),
        Codec.FLOAT.fieldOf("orbit_radius").orElse(1f).forGetter(OrbitBulletPathType::orbitRadius)
    )).apply(instance, OrbitBulletPathType::new));

    @Override
    public Type type() {
        return Type.ORBIT;
    }

    @Override
    public OrbitBulletPathInstance create() {
        return new OrbitBulletPathInstance(this);
    }

    @Override
    public Identifiable<String> withId(String id) {
        return new OrbitBulletPathType(id, sides, orbitRadius);
    }

    public static class OrbitBulletPathInstance extends Instance<OrbitBulletPathType> {
        private float orbitPhase;

        public OrbitBulletPathInstance(OrbitBulletPathType definition) {
            super(definition);
        }

        public void setOrbitPhase(float orbitPhase) {
            this.orbitPhase = orbitPhase;
        }

        @Override
        public void reset() {
            orbitPhase = 0f;
        }

        /**
         * Returns a point along a circle whose radius is defined by the {@link OrbitBulletPathType#orbitRadius}. The return value is treated as an offset, meaning it assumes the center of the circle is at (0,0).
         */
        @Override
        public float[] getOffset(BulletNode<?> node, float t) {
            float angle = (t / 1000f) * MathUtils.PI2 + orbitPhase;
            return new float[]{MathUtils.sin(angle) * getType().orbitRadius(), MathUtils.cos(angle) * getType().orbitRadius()};
        }
    }
}
