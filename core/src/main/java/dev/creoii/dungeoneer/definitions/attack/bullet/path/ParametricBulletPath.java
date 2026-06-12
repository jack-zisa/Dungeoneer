package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.SidedBullet;
import dev.creoii.dungeoneer.util.Codecs;

public record ParametricBulletPath(String id, BulletPathType type, ParametricType parametricType, Vector2 scale, boolean attached) implements BulletPath {
    public static final MapCodec<ParametricBulletPath> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return BulletPath.addDefaultFields(instance).and(instance.group(
            ParametricType.CODEC.fieldOf("parametric_type").orElse(ParametricType.FIGURE_EIGHT).forGetter(ParametricBulletPath::parametricType),
            Codecs.VECTOR_2.fieldOf("scale").orElse(Vector2.One).forGetter(ParametricBulletPath::scale),
            Codec.BOOL.fieldOf("attached").orElse(false).forGetter(ParametricBulletPath::attached)
        )).apply(instance, ParametricBulletPath::new);
    });

    @Override
    public void start(SidedBullet bullet, float dt, BulletPath previous) {
        bullet.resetDistanceTravelled();
        bullet.setSegmentStartAge(bullet.getAge());
    }

    @Override
    public void apply(SidedBullet bullet, float dt) {
        float t = (bullet.getAge() - bullet.getSegmentStartAge()) * MathUtils.PI2 * (bullet.getSpeed() / 1000f);

        float x = 0f;
        float y = 0f;
        switch (parametricType) {
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

        float localX = x * scale.x;
        float localY = y * scale.y;

        float dirX = bullet.getDirection().x;
        float dirY = bullet.getDirection().y;

        float perpX = -dirY;
        float perpY = dirX;

        float centerX;
        float centerY;

        if (attached && bullet.getAttached() != null) {
            centerX = bullet.getAttached().getCenterX();
            centerY = bullet.getAttached().getCenterY();
        } else {
            centerX = bullet.getStartX();
            centerY = bullet.getStartY();
        }

        bullet.getPos().set(centerX + perpX * localX + dirX * localY, centerY + perpY * localX + dirY * localY);
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
