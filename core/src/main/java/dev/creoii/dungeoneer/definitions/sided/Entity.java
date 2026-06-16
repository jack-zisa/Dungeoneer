package dev.creoii.dungeoneer.definitions.sided;

public interface Entity {
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
}
