package dev.creoii.dungeoneer.definitions.sided;

import com.badlogic.gdx.math.Vector2;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletDefinition;
import org.jspecify.annotations.Nullable;

public interface SidedBullet {
    BulletDefinition getDefinition();

    Vector2 getPos();

    float getStartX();

    float getStartY();

    Vector2 getDirection();

    float getSpeed();

    void incrementSpeed(float f);

    float getDistanceTravelled();

    void incrementDistanceTravelled(float f);

    float getLifetime();

    int getIndex();

    float getAge();

    float getOrbitPhase();

    float getAngleOffset();

    float getAngle();

    @Nullable
    SidedCharacter getAttached();
}
