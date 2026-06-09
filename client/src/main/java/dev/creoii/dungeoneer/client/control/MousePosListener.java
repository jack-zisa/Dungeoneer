package dev.creoii.dungeoneer.client.control;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public interface MousePosListener {
    Vector3 getMousePos();

    default void updateMousePos(Camera camera) {
        getMousePos().x = Gdx.input.getX();
        getMousePos().y = Gdx.input.getY();
        camera.unproject(getMousePos());
    }

    default Vector2 getDirectionToMouse(Vector2 from) {
        return new Vector2(getMousePos().x - from.x, getMousePos().y - from.y).nor();
    }

    default Vector2 getDirectionToMouse(float x, float y) {
        return new Vector2(getMousePos().x - x, getMousePos().y - y).nor();
    }
}
