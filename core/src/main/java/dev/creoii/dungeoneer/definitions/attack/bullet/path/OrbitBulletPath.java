package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.badlogic.gdx.math.MathUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.SidedBullet;

public record OrbitBulletPath(String id, BulletPathType type, float orbitRadius, boolean attached) implements BulletPath {
    public static final MapCodec<OrbitBulletPath> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return BulletPath.addDefaultFields(instance).and(instance.group(
            Codec.FLOAT.fieldOf("orbit_radius").orElse(1f).forGetter(OrbitBulletPath::orbitRadius),
            Codec.BOOL.fieldOf("attached").orElse(false).forGetter(OrbitBulletPath::attached)
        )).apply(instance, OrbitBulletPath::new);
    });

    @Override
    public void apply(SidedBullet bullet, float dt) {
        bullet.incrementSpeed(bullet.getDefinition().acceleration() * dt);
        bullet.incrementDistanceTravelled(bullet.getSpeed() * dt);

        float perpX = -bullet.getDirection().y;
        float perpY = bullet.getDirection().x;

        float orbitAngle = bullet.getAge() * MathUtils.PI2 * bullet.getDefinition().speed() + bullet.getOrbitPhase();
        float orbitForward = MathUtils.cos(orbitAngle) * orbitRadius;
        float orbitSide = MathUtils.sin(orbitAngle) * orbitRadius;

        float orbitX = bullet.getDirection().x * orbitForward + perpX * orbitSide;
        float orbitY = bullet.getDirection().y * orbitForward + perpY * orbitSide;

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
