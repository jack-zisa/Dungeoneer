package dev.creoii.dungeoneer.definitions.sided;

import com.badlogic.gdx.math.Vector2;
import dev.creoii.dungeoneer.util.stat.StatContainer;

public interface SidedCharacter {
    Vector2 getPos();

    Vector2 getVelocity();

    StatContainer getStats();

    default boolean canMove() {
        return getStats().speed().value() > 0f;
    }

    default boolean isMoving() {
        return !getVelocity().isZero();
    }
}
