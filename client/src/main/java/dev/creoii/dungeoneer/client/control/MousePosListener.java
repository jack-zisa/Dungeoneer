package dev.creoii.dungeoneer.client.control;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

public interface MousePosListener {
    Vector3 getMousePos();

    default void updateMousePos(Camera camera) {
        getMousePos().x = Gdx.input.getX();
        getMousePos().y = Gdx.input.getY();
        camera.unproject(getMousePos());
    }
}
