package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;
import dev.creoii.dungeoneer.util.Codecs;

public record ParametricBulletPathType(String id, ParametricType parametricType, Vector2 scale) implements BulletPathType<ParametricBulletPathType.ParametricBulletPathInstance> {
    public static final MapCodec<ParametricBulletPathType> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> BulletPathType.addDefaultFields(instance).and(instance.group(
        ParametricType.CODEC.fieldOf("parametric_type").orElse(ParametricType.FIGURE_EIGHT).forGetter(ParametricBulletPathType::parametricType),
        Codecs.VECTOR_2.fieldOf("scale").orElse(Vector2.One).forGetter(ParametricBulletPathType::scale)
    )).apply(instance, ParametricBulletPathType::new));

    @Override
    public Type type() {
        return Type.PARAMETRIC;
    }

    @Override
    public ParametricBulletPathInstance create() {
        return new ParametricBulletPathInstance(this);
    }

    public static class ParametricBulletPathInstance extends Instance<ParametricBulletPathType> {
        public ParametricBulletPathInstance(ParametricBulletPathType definition) {
            super(definition);
        }

        @Override
        public float[] getOffset(BulletNode<?> node, float t) {
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
                    float r = t * .1f;
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
            return new float[] {x * getType().scale.x, y * getType().scale.y};
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
