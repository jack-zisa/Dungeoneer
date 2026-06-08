package dev.creoii.dungeoneer.client.control;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import dev.creoii.dungeoneer.client.Dungeoneer;

public class DungeonEditorInputListener extends InputListener {
    private final Dungeoneer client;
    private final OrthographicCamera camera;
    private boolean dragging;
    private float lastX;
    private float lastY;

    public DungeonEditorInputListener(Dungeoneer client, OrthographicCamera camera) {
        this.client = client;
        this.camera = camera;
        camera.update();
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        if (button == Input.Buttons.RIGHT) {
            dragging = true;
            lastX = x;
            lastY = y;
            return true;
        }

        return false;
    }

    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer) {
        if (!dragging) return;

        float dx = x - lastX;
        float dy = y - lastY;

        camera.position.x -= dx * camera.zoom;
        camera.position.y -= dy * camera.zoom;

        lastX = x;
        lastY = y;

        camera.update();
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        if (button == Input.Buttons.RIGHT) {
            dragging = false;
        }
    }
}
