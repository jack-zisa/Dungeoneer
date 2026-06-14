package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;
import dev.creoii.dungeoneer.util.Codecs;

public record ParametricBulletPathType(String id, ParametricType parametricType, Vector2 scale) implements BulletPathType<ParametricBulletPathType.ParametricBulletPathInstance> {
    public static final MapCodec<ParametricBulletPathType> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return BulletPathType.addDefaultFields(instance).and(instance.group(
            ParametricType.CODEC.fieldOf("parametric_type").orElse(ParametricType.FIGURE_EIGHT).forGetter(ParametricBulletPathType::parametricType),
            Codecs.VECTOR_2.fieldOf("scale").orElse(Vector2.One).forGetter(ParametricBulletPathType::scale)
        )).apply(instance, ParametricBulletPathType::new);
    });

    @Override
    public Type type() {
        return Type.PARAMETRIC;
    }

    @Override
    public ParametricBulletPathInstance create() {
        return new ParametricBulletPathInstance(this);
    }

    public static class ParametricBulletPathInstance extends Instance<ParametricBulletPathType> implements AngledBulletPath {
        private float angle;

        public ParametricBulletPathInstance(ParametricBulletPathType definition) {
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

        @Override
        public void reset() {
            angle = 0f;
        }

        @Override
        public void start(BulletNode bullet, float dt, Instance<?> previous) {
            bullet.resetDistanceTravelled();

            if (bullet.getPath() instanceof SegmentedBulletPathType.SegmentedBulletPathInstance instance)
                instance.setSegmentStartAge(bullet.getAge());
        }

        @Override
        public void update(BulletNode bullet, float dt) {
            float age = bullet.getAge();
            if (bullet.getPath() instanceof SegmentedBulletPathType.SegmentedBulletPathInstance instance)
                age -= instance.getSegmentStartAge();

            float t = age * MathUtils.PI2 * (bullet.getSpeed() / 1000f);

            float x = 0f;
            float y = 0f;
            switch (getType().parametricType) {
                case CIRCLE -> {
                    x = MathUtils.cos(t);
                    y = MathUtils.sin(t);
                }
                case FIGURE_EIGHT -> {
                    x = MathUtils.sin(t);
                    y = MathUtils.sin(t * 2f);
                }
                case SPIRAL -> {
                    float r = t * 0.1f;

                    x = MathUtils.cos(t) * r;
                    y = MathUtils.sin(t) * r;
                }
                case ROSE -> {
                    float r = MathUtils.cos(5f * t);
                    x = r * MathUtils.cos(t);
                    y = r * MathUtils.sin(t);
                }
                case HEART -> {
                    x = 16f * MathUtils.sin(t) * MathUtils.sin(t) * MathUtils.sin(t);
                    y = 13f * MathUtils.cos(t) - 5f * MathUtils.cos(2f * t) - 2f * MathUtils.cos(3f * t) - MathUtils.cos(4f * t);
                    x /= 16f;
                    y /= 16f;
                }
                case ASTROID -> {
                    x = MathUtils.cos(t);
                    x = x * x * x;
                    y = MathUtils.sin(t);
                    y = y * y * y;
                }
                case LISSAJOUS -> {
                    x = MathUtils.sin(3f * t + MathUtils.PI / 2f);
                    y = MathUtils.sin(2f * t);
                }
            }

            float localX = x * getType().scale.x;
            float localY = y * getType().scale.y;

            float dirX = bullet.getDirX();
            float dirY = bullet.getDirY();

            float perpX = -dirY;
            float perpY = dirX;

            bullet.setLocalPos(perpX * localX + dirX * localY, perpY * localX + dirY * localY);
        }
    }

    public enum ParametricType {
        CIRCLE,
        FIGURE_EIGHT,
        SPIRAL,
        ROSE,
        HEART,
        ASTROID,
        LISSAJOUS;

        public static final Codec<ParametricType> CODEC = Codec.STRING.xmap(s -> ParametricType.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    }
}
