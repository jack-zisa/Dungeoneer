package dev.creoii.dungeoneer.definitions.sided;

import com.badlogic.gdx.math.Vector2;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletDefinition;
import org.jspecify.annotations.Nullable;

public interface SidedBullet {
    BulletDefinition getDefinition();

    Vector2 getPos();

    float getStartX();

    float getStartY();

    void setStartPos(float x, float y);

    Vector2 getDirection();

    float getSpeed();

    float getDistanceTravelled();

    void resetDistanceTravelled();

    float getLifetime();

    int getIndex();

    float getAge();

    float getOrbitPhase();

    float getAngleOffset();

    float getAngle();

    @Nullable
    Character getAttached();

    float getCurrentSegmentThreshold();

    void setCurrentSegmentThreshold(float threshold);

    float getSegmentStartAge();

    void setSegmentStartAge(float segmentStartAge);
}
