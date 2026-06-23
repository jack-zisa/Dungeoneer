package dev.creoii.dungeoneer.util.collision;

import com.badlogic.gdx.math.Rectangle;

public interface Collidable {
    Rectangle getBounds();

    default void onCollisionEnter(Collidable other) {
    }

    default void onCollision(Collidable other) {
    }

    default void onCollisionExit(Collidable other) {
    }
}
