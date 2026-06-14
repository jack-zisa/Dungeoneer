package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.badlogic.gdx.math.MathUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;

public record OrbitBulletPathType(String id, int sides, float orbitRadius) implements BulletPathType<OrbitBulletPathType.OrbitBulletPathInstance> {
    public static final MapCodec<OrbitBulletPathType> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return BulletPathType.addDefaultFields(instance).and(instance.group(
            Codec.INT.fieldOf("sides").orElse(-1).forGetter(OrbitBulletPathType::sides),
            Codec.FLOAT.fieldOf("orbit_radius").orElse(1f).forGetter(OrbitBulletPathType::orbitRadius)
        )).apply(instance, OrbitBulletPathType::new);
    });

    @Override
    public Type type() {
        return Type.ORBIT;
    }

    @Override
    public OrbitBulletPathInstance create() {
        return new OrbitBulletPathInstance(this);
    }

    public static class OrbitBulletPathInstance extends Instance<OrbitBulletPathType> implements AngledBulletPath {
        private float angle;
        private float orbitPhase;

        public OrbitBulletPathInstance(OrbitBulletPathType definition) {
            super(definition);
        }

        @Override
        public float getAngle() {
            return angle;
        }

        @Override
        public void incrementAngle(float f) {
            angle += f;
        }

        public float getOrbitPhase() {
            return orbitPhase;
        }

        public void setOrbitPhase(float orbitPhase) {
            this.orbitPhase = orbitPhase;
        }

        @Override
        public void reset() {
            angle = 0f;
            orbitPhase = 0f;
        }

        @Override
        public float[] getOffset(BulletNode bullet, float dt) {
            float age = bullet.getAge();

            float orbitAngle =
                age * MathUtils.PI2 * (bullet.getSpeed() / 1000f)
                    + orbitPhase;

            if (getType().sides > 0) {
                float t = orbitAngle / MathUtils.PI2;
                t -= (float)Math.floor(t);

                float scaled = t * getType().sides;

                int edge = (int)scaled;
                float u = scaled - edge;

                float angle1 = edge * MathUtils.PI2 / getType().sides;
                float angle2 = ((edge + 1) % getType().sides)
                    * MathUtils.PI2 / getType().sides;

                float side1 =
                    MathUtils.cos(angle1) * getType().orbitRadius;
                float forward1 =
                    MathUtils.sin(angle1) * getType().orbitRadius;

                float side2 =
                    MathUtils.cos(angle2) * getType().orbitRadius;
                float forward2 =
                    MathUtils.sin(angle2) * getType().orbitRadius;

                return new float[] {
                    MathUtils.lerp(side1, side2, u),
                    MathUtils.lerp(forward1, forward2, u)
                };
            }

            return new float[] {
                MathUtils.sin(orbitAngle) * getType().orbitRadius,
                MathUtils.cos(orbitAngle) * getType().orbitRadius
            };
        }
    }
}
