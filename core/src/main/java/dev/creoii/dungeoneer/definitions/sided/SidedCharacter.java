package dev.creoii.dungeoneer.definitions.sided;

import com.badlogic.gdx.math.Vector2;

public interface SidedCharacter {
    Vector2 getPos();

    Vector2 getVelocity();

    float getSpeed();

    default boolean canMove() {
        return getSpeed() > 0f;
    }

    default boolean isMoving() {
        return !getVelocity().isZero();
    }
}
