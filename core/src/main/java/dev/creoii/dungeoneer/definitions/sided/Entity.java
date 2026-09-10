package dev.creoii.dungeoneer.definitions.sided;

import dev.creoii.dungeoneer.util.collision.Collidable;

public interface Entity extends Collidable {
    float[] getPos();

    default float getX() {
        return getPos()[0];
    }

    default float getY() {
        return getPos()[1];
    }

    default void setPos(float x, float y) {
        getPos()[0] = x;
        getPos()[1] = y;
    }

    float getCenterX();

    float getCenterY();

    void setDead(boolean dead);

    boolean isDead();

    default void onTileCollision() {
    }
}
