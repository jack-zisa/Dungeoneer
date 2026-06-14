package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;

import java.util.ArrayList;
import java.util.List;

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

        public void setOrbitPhase(float orbitPhase) {
            this.orbitPhase = orbitPhase;
        }

        @Override
        public void reset() {
        }

        @Override
        public void start(BulletNode bullet, float dt, Instance<?> previous) {
            bullet.resetDistanceTravelled();
            if (bullet.getPath() instanceof SegmentedBulletPathType.SegmentedBulletPathInstance instance)
                instance.setSegmentStartAge(bullet.getAge());
        }

        @Override
        public void update(BulletNode bullet, float dt) {
            float perpX = -bullet.getDirY();
            float perpY = bullet.getDirX();

            float age = bullet.getAge();
            if (bullet.getPath() instanceof SegmentedBulletPathType.SegmentedBulletPathInstance instance)
                age -= instance.getSegmentStartAge();

            float orbitAngle = age * MathUtils.PI2 * (bullet.getSpeed() / 1000f) + orbitPhase;

            float orbitX;
            float orbitY;
            if (getType().sides > 0) {
                List<Vector2> vertices = new ArrayList<>();
                for (int i = 0; i < getType().sides; i++) {
                    float a = i * MathUtils.PI2 / getType().sides;
                    vertices.add(new Vector2(MathUtils.cos(a) * getType().orbitRadius, MathUtils.sin(a) * getType().orbitRadius));
                }

                float t = (orbitAngle / MathUtils.PI2) % 1f;
                float scaled = t * getType().sides;

                int edge = (int) scaled;
                float u = scaled - edge;

                Vector2 v1 = vertices.get(edge);
                Vector2 v2 = vertices.get((edge + 1) % getType().sides);

                float x = MathUtils.lerp(v1.x, v2.x, u);
                float y = MathUtils.lerp(v1.y, v2.y, u);

                orbitX = bullet.getDirX() * x + perpX * y;
                orbitY = bullet.getDirY() * x + perpY * y;
            } else {
                float orbitForward = MathUtils.cos(orbitAngle) * getType().orbitRadius;
                float orbitSide = MathUtils.sin(orbitAngle) * getType().orbitRadius;

                orbitX = bullet.getDirX() * orbitForward + perpX * orbitSide;
                orbitY = bullet.getDirY() * orbitForward + perpY * orbitSide;
            }

            float centerX;
            float centerY;

            if (bullet.getParent() != null) {
                centerX = bullet.getParent().getX();
                centerY = bullet.getParent().getY();
            } else {
                centerX = bullet.getStartX();
                centerY = bullet.getStartY();
            }

            bullet.setPos(centerX + orbitX, centerY + orbitY);
        }
    }
}
