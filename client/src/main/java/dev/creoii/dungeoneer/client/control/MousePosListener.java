package dev.creoii.dungeoneer.client.control;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import dev.creoii.dungeoneer.util.VectorUtils;

public interface MousePosListener {
    float[] getMousePos();

    default void updateMousePos(Camera camera, float rotation) {
        getMousePos()[0] = Gdx.input.getX();
        getMousePos()[1] = Gdx.input.getY();

        float x = getMousePos()[0],
            y = Gdx.graphics.getHeight() - getMousePos()[1];
        getMousePos()[0] = (2 * x) / Gdx.graphics.getWidth() - 1;
        getMousePos()[1] = (2 * y) / Gdx.graphics.getHeight() - 1;
        VectorUtils.prj(getMousePos(), camera.invProjectionView);

        getMousePos()[0] -= camera.position.x;
        getMousePos()[1] -= camera.position.y;
        VectorUtils.rotateDeg(getMousePos(), -rotation);
        getMousePos()[0] += camera.position.x;
        getMousePos()[1] += camera.position.y;
    }

    default float[] getDirectionToMouse(float x, float y) {
        float[] mouseDir = new float[]{getMousePos()[0] - x, getMousePos()[1] - y};
        VectorUtils.nor(mouseDir);
        return mouseDir;
    }
}
