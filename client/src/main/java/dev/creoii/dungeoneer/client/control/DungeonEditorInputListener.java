package dev.creoii.dungeoneer.client.control;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.client.editor.action.CompositeAction;
import dev.creoii.dungeoneer.client.editor.action.SetTileAction;
import dev.creoii.dungeoneer.client.editor.selection.AreaSelection;
import dev.creoii.dungeoneer.client.editor.action.EditorAction;
import dev.creoii.dungeoneer.client.screen.editor.DungeonEditorScreen;
import dev.creoii.dungeoneer.client.screen.editor.Tiles;
import dev.creoii.dungeoneer.client.util.InputUtils;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.UndoRedoList;
import dev.creoii.dungeoneer.util.VectorUtils;

import java.awt.*;
import java.util.Random;

public class DungeonEditorInputListener extends InputAdapter implements MousePosListener {
    private static final float[] ZOOM_LEVELS = {.2f, .25f, .35f, .5f, .7f, .95f, 1.25f, 1.6f, 2f, 2.45f};
    private final DungeonEditorScreen screen;
    private final UndoRedoList<EditorAction> undoRedoList;
    private CompositeAction currentActions;
    private boolean dragging;
    private boolean selecting;
    private float lastX;
    private float lastY;
    private final float[] mousePos;
    private int screenX;

    public DungeonEditorInputListener(DungeonEditorScreen screen) {
        this.screen = screen;
        undoRedoList = new UndoRedoList<>();
        currentActions = new CompositeAction();
        mousePos = VectorUtils.zero();
        screen.getCamera().update();
    }

    @Override
    public float[] getMousePos() {
        return mousePos;
    }

    public boolean isSelecting() {
        return selecting;
    }

    public void setSelecting(boolean selecting) {
        this.selecting = selecting;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE && selecting && screen.getSidebar().getSelection() != null) {
            selecting = false;
            screen.getSidebar().getSelection().clear();
            return true;
        }

        if (keycode == Input.Keys.Z && InputUtils.isCtrl()) {
            if (undoRedoList.canUndo()) {
                EditorAction undoAction = undoRedoList.undo();
                if (undoAction != null) undoAction.undo();
            }
            return true;
        }

        if (keycode == Input.Keys.Y && InputUtils.isCtrl()) {
            if (undoRedoList.canRedo()) {
                EditorAction redoAction = undoRedoList.redo();
                if (redoAction != null) redoAction.redo();
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        lastX = screenX;
        lastY = screenY;

        if (button == Input.Buttons.RIGHT) {
            dragging = true;
            return true;
        }

        if (button == Input.Buttons.LEFT) {
            Point point = screen.getHoveredPos();
            if (InputUtils.isCtrl() && screen.getSidebar().getSelection() instanceof AreaSelection areaSelection) {
                selecting = true;
                areaSelection.setMin(point.x, point.y);
                areaSelection.setMax(point.x, point.y);
                return true;
            } else if (!selecting) {
                TiledMapTileLayer tileLayer = (TiledMapTileLayer) screen.getMapRenderer().getMap().getLayers().get(Constants.MAP_LAYER_GROUND);
                if (point != null && screen.getSidebar().getSelectedTile() != null) {
                    placeTilesAt(tileLayer, point.x, point.y);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Point point = screen.getHoveredPos();

        if (dragging) {
            float dx = screenX - lastX;
            float dy = screenY - lastY;

            screen.getCamera().position.x -= dx * screen.getCamera().zoom;
            screen.getCamera().position.y -= dy * screen.getCamera().zoom;

            screen.getCamera().update();
        }

        lastX = screenX;
        lastY = screenY;

        if (!selecting && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && point != null && screen.getSidebar().getSelectedTile() != null) {
            TiledMapTileLayer tileLayer = (TiledMapTileLayer) screen.getMapRenderer().getMap().getLayers().get(Constants.MAP_LAYER_GROUND);
            placeTilesAt(tileLayer, point.x, point.y);
        }

        if (selecting && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && InputUtils.isCtrl() && screen.getSidebar().getSelection() instanceof AreaSelection areaSelection && point != null) {
            areaSelection.setMax(point.x, point.y);
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.RIGHT) dragging = false;

        if (!selecting && button == Input.Buttons.LEFT && currentActions.size() > 0) {
            undoRedoList.add(currentActions);
            currentActions = new CompositeAction();
        }
        return false;
    }

    public void placeTilesAt(TiledMapTileLayer tileLayer, int x, int y) {
        if (screen.getSidebar().getBrushSize() == 1) {
            TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
            if (cell == null) {
                cell = new TiledMapTileLayer.Cell();
            }

            TiledMapTile old = cell.getTile();
            SetTileAction action = new SetTileAction(tileLayer, x, y, old == null ? null : DataManager.getTile(Tiles.TILES.inverse().get(old)), screen.getSidebar().getSelectedTile().get(new Random()));
            action.redo();
            currentActions.add(action);
        } else {
            int radius = screen.getSidebar().getBrushSize() - 1;

            CompositeAction compositeAction = new CompositeAction();
            for (int yo = -radius; yo <= radius; ++yo) {
                for (int xo = -radius; xo <= radius; ++xo) {
                    TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
                    if (cell == null) {
                        cell = new TiledMapTileLayer.Cell();
                    }

                    TiledMapTile old = cell.getTile();
                    SetTileAction action = new SetTileAction(tileLayer, x + xo, y + yo, old == null ? null : DataManager.getTile(Tiles.TILES.inverse().get(old)), screen.getSidebar().getSelectedTile().get(new Random()));
                    action.redo();
                    compositeAction.add(action);
                }
            }
            currentActions.add(compositeAction);
        }
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        updateZoom(amountY);
        return false;
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
