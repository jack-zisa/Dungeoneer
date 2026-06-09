package dev.creoii.dungeoneer.client.control;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import dev.creoii.dungeoneer.client.screen.editor.DungeonEditorScreen;
import dev.creoii.dungeoneer.client.screen.editor.Tiles;

import java.awt.*;

public class DungeonEditorInputListener extends InputListener implements MousePosListener {
    private static final float[] ZOOM_LEVELS = {.25f, .35f, .5f, .7f, .95f, 1.25f, 1.6f, 2f};
    private final DungeonEditorScreen screen;
    private boolean dragging;
    private float lastX;
    private float lastY;
    private final Vector3 mousePos;

    public DungeonEditorInputListener(DungeonEditorScreen screen) {
        this.screen = screen;
        mousePos = new Vector3();
        screen.getCamera().update();
    }

    @Override
    public Vector3 getMousePos() {
        return mousePos;
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        if (button == Input.Buttons.RIGHT) {
            dragging = true;
            lastX = x;
            lastY = y;
            return true;
        } else if (button == Input.Buttons.LEFT) {
            TiledMapTileLayer tileLayer = (TiledMapTileLayer) screen.getMapRenderer().getMap().getLayers().get("ground");
            Point point = screen.getHoveredPos();
            if (point != null) {
                TiledMapTileLayer.Cell cell = tileLayer.getCell(point.x, point.y);
                if (cell == null) {
                    cell = new TiledMapTileLayer.Cell();
                    if (screen.getSidebar().getSelectedTile() != null) {
                        cell.setTile(screen.getSidebar().getSelectedTile());
                        tileLayer.setCell(point.x, point.y, cell);
                    }
                } else if (screen.getSidebar().getSelectedTile() != null) {
                    cell.setTile(screen.getSidebar().getSelectedTile());
                } else cell.setTile(null);
            }
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
