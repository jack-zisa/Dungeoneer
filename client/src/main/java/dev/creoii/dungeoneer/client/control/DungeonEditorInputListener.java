package dev.creoii.dungeoneer.client.control;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import dev.creoii.dungeoneer.client.screen.DungeonEditorScreen;

public class DungeonEditorInputListener extends InputListener {
    private static final float[] ZOOM_LEVELS = {.25f, .35f, .5f, .7f, .95f, 1.25f, 1.6f, 2f};
    private final DungeonEditorScreen screen;
    private boolean dragging;
    private float lastX;
    private float lastY;

    public DungeonEditorInputListener(DungeonEditorScreen screen) {
        this.screen = screen;
        screen.getCamera().update();
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

        screen.getCamera().position.x -= dx * screen.getCamera().zoom;
        screen.getCamera().position.y -= dy * screen.getCamera().zoom;

        lastX = x;
        lastY = y;

        screen.getCamera().update();
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        if (button == Input.Buttons.RIGHT) {
            dragging = false;
        }
    }

    @Override
    public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
        updateZoom(amountY);
        return super.scrolled(event, x, y, amountX, amountY);
    }

    private int getClosestZoomIndex(float zoom) {
        int closest = 0;

        for (int i = 1; i < ZOOM_LEVELS.length; i++) {
            if (Math.abs(ZOOM_LEVELS[i] - zoom) < Math.abs(ZOOM_LEVELS[closest] - zoom)) {
                closest = i;
            }
        }

        return closest;
    }

    public void updateZoom(float amountY) {
        int index = getClosestZoomIndex(screen.getCamera().zoom);
        if (amountY > 0 && index < ZOOM_LEVELS.length - 1) {
            screen.getCamera().zoom = ZOOM_LEVELS[index + 1];
            screen.getCamera().update();
        } else if (amountY < 0 && index > 0) {
            screen.getCamera().zoom = ZOOM_LEVELS[index - 1];
            screen.getCamera().update();
        }
    }
}
