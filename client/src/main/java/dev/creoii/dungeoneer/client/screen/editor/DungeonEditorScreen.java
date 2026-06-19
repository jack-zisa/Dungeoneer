package dev.creoii.dungeoneer.client.screen.editor;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.control.DungeonEditorInputListener;
import dev.creoii.dungeoneer.client.editor.selection.AreaSelection;
import dev.creoii.dungeoneer.client.screen.AbstractScreen;
import dev.creoii.dungeoneer.client.screen.main.MainScreen;
import dev.creoii.dungeoneer.util.Constants;

import java.awt.*;

public class DungeonEditorScreen extends AbstractScreen {
    private final DungeonMapManager mapManager;
    private DungeonEditorInputListener inputListener;
    private OrthographicCamera camera;
    private OrthogonalTiledMapRenderer mapRenderer;
    private ShapeRenderer shapeRenderer;

    private Sidebar sidebar;
    private Label hoverPosLabel;

    public DungeonEditorScreen(Dungeoneer client) {
        super(client);
        mapManager = new DungeonMapManager(client);
    }

    public DungeonMapManager getMapManager() {
        return mapManager;
    }

    public DungeonEditorInputListener getInputListener() {
        return inputListener;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public OrthogonalTiledMapRenderer getMapRenderer() {
        return mapRenderer;
    }

    public void setMapRenderer(OrthogonalTiledMapRenderer mapRenderer) {
        this.mapRenderer = mapRenderer;
        mapManager.init(mapRenderer.getMap());
    }

    public Sidebar getSidebar() {
        return sidebar;
    }

    public TiledMap buildEmptyMap() {
        TiledMap map = new TiledMap();
        TiledMapTileLayer ground = new TiledMapTileLayer(256, 256, 8, 8);
        ground.setName(Constants.MAP_LAYER_GROUND);
        map.getLayers().add(ground);
        return map;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.zoom = 1f;

        setMapRenderer(new OrthogonalTiledMapRenderer(getClient().getState().getDungeonMap() == null ? buildEmptyMap() : mapManager.read(getClient().getState().getDungeonMap().mapData())));

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);

        Table root = new Table();
        root.setFillParent(true);

        sidebar = new Sidebar(this);

        Table content = new Table();
        content.add(hoverPosLabel = new Label("", SKIN)).left().row();

        Table buttons = new Table();
        TextButton finishButton = new TextButton("Finish", SKIN);
        finishButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                mapManager.save();
                getClient().setScreen(new MainScreen(getClient()));
                getClient().getState().setStatus(ClientState.Status.LOBBY);
            }
        });
        TextButton cancelButton = new TextButton("Cancel", SKIN);
        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getClient().setScreen(new MainScreen(getClient()));
                getClient().getState().setStatus(ClientState.Status.LOBBY);
            }
        });
        buttons.add(finishButton);
        buttons.add(cancelButton).padLeft(5f);

        content.add(buttons).expandY().bottom();

        root.add(sidebar).width(200f).fillY().left().top();
        root.add(content).grow();

        getStage().addActor(root);
        getClient().getInputMultiplexer().addProcessor(getStage());
        getClient().getInputMultiplexer().addProcessor(inputListener = new DungeonEditorInputListener(this));
        super.show();
    }

    @Override
    public void hide() {
        getClient().getInputMultiplexer().removeProcessor(getStage());
        getClient().getInputMultiplexer().removeProcessor(inputListener);
    }

    @Override
    public void render(float delta) {
        mapRenderer.setView(camera);
        mapRenderer.render();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin();
        shapeRenderer.setColor(1f, 0f, 1f, 1f);
        shapeRenderer.rect(0f, 0f, 256f * 8f, 256f * 8f); // editor bounds
        shapeRenderer.setColor(0f, 1f, 1f, 1f);
        if (sidebar.getSelection() != null && inputListener.isSelecting()) {
            switch (sidebar.getSelection().getType()) {
                case AREA -> {
                    AreaSelection areaSelection = (AreaSelection) sidebar.getSelection();
                    shapeRenderer.rect(areaSelection.area.x * 8f, areaSelection.area.y * 8f, areaSelection.area.width * 8f, areaSelection.area.height * 8f);
                }
            }
        } else {
            Point hover = getHoveredPos();
            if (hover.getX() >= 0f && hover.getY() >= 0f && hover.getX() < 256f && hover.getY() < 256f) {
                int radius = sidebar.getBrushSize() - 1;
                float x = (hover.x - radius) * 8f;
                float y = (hover.y - radius) * 8f;
                float size = (radius * 2f + 1f) * 8f;
                shapeRenderer.rect(x, y, size, size);
            }
        }
        shapeRenderer.end();

        inputListener.updateMousePos(camera);
        if (inputListener != null) {
            hoverPosLabel.setText(getHoveredPos().x + "," + getHoveredPos().y);
        }

        super.render(delta);
    }

    @Override
    public void dispose() {
        super.dispose();
        SKIN.dispose();
    }

    public Point getHoveredPos() {
        float[] mousePos = inputListener.getMousePos();
        return new Point((int) Math.floor(mousePos[0] / 8f), (int) Math.floor(mousePos[1] / 8f));
    }
}
