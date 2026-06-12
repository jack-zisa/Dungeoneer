package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.SidedBullet;

import java.util.ArrayList;
import java.util.List;

public record OrbitBulletPath(String id, int sides, float orbitRadius, boolean attached) implements BulletPath {
    public static final MapCodec<OrbitBulletPath> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return BulletPath.addDefaultFields(instance).and(instance.group(
            Codec.INT.fieldOf("sides").orElse(-1).forGetter(OrbitBulletPath::sides),
            Codec.FLOAT.fieldOf("orbit_radius").orElse(1f).forGetter(OrbitBulletPath::orbitRadius),
            Codec.BOOL.fieldOf("attached").orElse(false).forGetter(OrbitBulletPath::attached)
        )).apply(instance, OrbitBulletPath::new);
    });

    @Override
    public BulletPathType type() {
        return BulletPathType.ORBIT;
    }

    @Override
    public void start(SidedBullet bullet, float dt, BulletPath previous) {
        bullet.resetDistanceTravelled();
        bullet.setSegmentStartAge(bullet.getAge());
    }

    @Override
    public void apply(SidedBullet bullet, float dt) {
        float perpX = -bullet.getDirection().y;
        float perpY = bullet.getDirection().x;

        float orbitAngle = (bullet.getAge() - bullet.getSegmentStartAge()) * MathUtils.PI2 * (bullet.getDefinition().speed() / 1000f) + bullet.getOrbitPhase();

        float orbitX;
        float orbitY;
        if (sides > 0) {
            List<Vector2> vertices = new ArrayList<>();
            for (int i = 0; i < sides; i++) {
                float a = i * MathUtils.PI2 / sides;
                vertices.add(new Vector2(MathUtils.cos(a) * orbitRadius, MathUtils.sin(a) * orbitRadius));
            }

            float t = (orbitAngle / MathUtils.PI2) % 1f;
            float scaled = t * sides;

            int edge = (int) scaled;
            float u = scaled - edge;

            Vector2 v1 = vertices.get(edge);
            Vector2 v2 = vertices.get((edge + 1) % sides);

            float x = MathUtils.lerp(v1.x, v2.x, u);
            float y = MathUtils.lerp(v1.y, v2.y, u);

            orbitX = bullet.getDirection().x * x + perpX * y;
            orbitY = bullet.getDirection().y * x + perpY * y;
        } else {
            float orbitForward = MathUtils.cos(orbitAngle) * orbitRadius;
            float orbitSide = MathUtils.sin(orbitAngle) * orbitRadius;

            orbitX = bullet.getDirection().x * orbitForward + perpX * orbitSide;
            orbitY = bullet.getDirection().y * orbitForward + perpY * orbitSide;
        }

        float centerX;
        float centerY;

        if (attached && bullet.getAttached() != null) {
            centerX = bullet.getAttached().getCenterX();
            centerY = bullet.getAttached().getCenterY();
        } else {
            centerX = bullet.getStartX();
            centerY = bullet.getStartY();
        }

        bullet.getPos().set(centerX + orbitX, centerY + orbitY);
    }
}
