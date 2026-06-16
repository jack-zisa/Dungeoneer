package dev.creoii.dungeoneer.definitions.sided;

import dev.creoii.dungeoneer.util.stat.StatContainer;

public interface Character extends Entity {
    float[] getVelocity();

    default void setVelocity(float x, float y) {
        getVelocity()[0] = x;
        getVelocity()[1] = y;
    }

    StatContainer getStats();

    default boolean canMove() {
        return getStats().speed().value() > 0f;
    }

    default boolean isMoving() {
        return getVelocity()[0] != 0f || getVelocity()[1] != 0f;
    }
}
