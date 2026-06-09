package dev.creoii.dungeoneer.client.control;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import dev.creoii.dungeoneer.client.editor.AreaSelection;
import dev.creoii.dungeoneer.client.screen.editor.DungeonEditorScreen;

import java.awt.*;

public class DungeonEditorInputListener extends InputListener implements MousePosListener {
    private static final float[] ZOOM_LEVELS = {.25f, .35f, .5f, .7f, .95f, 1.25f, 1.6f, 2f, 2.45f};
    private final DungeonEditorScreen screen;
    private boolean dragging;
    private boolean selecting;
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

    public boolean isSelecting() {
        return selecting;
    }

    public void setSelecting(boolean selecting) {
        this.selecting = selecting;
    }

    @Override
    public boolean keyDown(InputEvent event, int keycode) {
        if (keycode == Input.Keys.ESCAPE && selecting && screen.getSidebar().getSelection() != null) {
            selecting = false;
            screen.getSidebar().getSelection().clear();
        }
        return false;
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        Actor target = event.getTarget();
        if (target != null && target != screen.getStage().getRoot()) {
            return false;
        }

        lastX = x;
        lastY = y;

        if (button == Input.Buttons.RIGHT) {
            dragging = true;
            return true;
        }

        if (button == Input.Buttons.LEFT) {
            Point point = screen.getHoveredPos();
            if ((Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) && screen.getSidebar().getSelection() instanceof AreaSelection areaSelection) {
                selecting = true;

                areaSelection.setMin(point.x, point.y);
                areaSelection.setMax(point.x, point.y);
                return true;
            } else {
                TiledMapTileLayer tileLayer = (TiledMapTileLayer) screen.getMapRenderer().getMap().getLayers().get("ground");
                if (point != null) {
                    setTileAt(tileLayer, point.x, point.y);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer) {
        Point point = screen.getHoveredPos();

        if (dragging) {
            float dx = x - lastX;
            float dy = y - lastY;

            screen.getCamera().position.x -= dx * screen.getCamera().zoom;
            screen.getCamera().position.y -= dy * screen.getCamera().zoom;

            screen.getCamera().update();
        }

        lastX = x;
        lastY = y;

        if (!selecting && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && point != null) {
            TiledMapTileLayer tileLayer = (TiledMapTileLayer) screen.getMapRenderer().getMap().getLayers().get("ground");
            setTileAt(tileLayer, point.x, point.y);
        }

        if (selecting && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && screen.getSidebar().getSelection() instanceof AreaSelection areaSelection && point != null) {
            areaSelection.setMax(point.x, point.y);
        }
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        if (button == Input.Buttons.RIGHT) dragging = false;
    }

    public void setTileAt(TiledMapTileLayer tileLayer, int x, int y) {
        TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
        if (cell == null) {
            cell = new TiledMapTileLayer.Cell();
            if (screen.getSidebar().getSelectedTile() != null) {
                cell.setTile(screen.getSidebar().getSelectedTile());
                tileLayer.setCell(x, y, cell);
            }
        } else if (screen.getSidebar().getSelectedTile() != null) {
            cell.setTile(screen.getSidebar().getSelectedTile());
        } else cell.setTile(null);
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
