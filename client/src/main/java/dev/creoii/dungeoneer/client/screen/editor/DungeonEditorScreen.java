package dev.creoii.dungeoneer.client.screen.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.control.DungeonEditorInputListener;
import dev.creoii.dungeoneer.client.editor.selection.AreaSelection;
import dev.creoii.dungeoneer.client.screen.AbstractScreen;
import dev.creoii.dungeoneer.client.screen.main.MainScreen;
import dev.creoii.dungeoneer.util.Constants;

import java.awt.*;

public class DungeonEditorScreen extends AbstractScreen {
    private final Dungeoneer client;
    private final DungeonMapManager mapManager;
    private DungeonEditorInputListener inputListener;
    private OrthographicCamera camera;
    private OrthogonalTiledMapRenderer mapRenderer;
    private ShapeRenderer shapeRenderer;

    private Sidebar sidebar;
    private Label hoverPosLabel;

    public DungeonEditorScreen(Dungeoneer client) {
        this.client = client;
        mapManager = new DungeonMapManager(client);
    }

    public Dungeoneer getClient() {
        return client;
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

        inputListener = new DungeonEditorInputListener(this);

        setMapRenderer(new OrthogonalTiledMapRenderer(client.getState().getDungeonMap() == null ? buildEmptyMap() : mapManager.read(client.getState().getDungeonMap().mapData())));

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
                client.setScreen(new MainScreen(client));
                client.getState().setStatus(ClientState.Status.LOBBY);
            }
        });
        TextButton cancelButton = new TextButton("Cancel", SKIN);
        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                client.setScreen(new MainScreen(client));
                client.getState().setStatus(ClientState.Status.LOBBY);
            }
        });
        buttons.add(finishButton);
        buttons.add(cancelButton).padLeft(5f);

        content.add(buttons).expandY().bottom();

        root.add(sidebar).width(200f).fillY().left().top();
        root.add(content).grow();

        getStage().addActor(root);
        getStage().addListener(inputListener);
        super.show();
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

        if (inputListener != null) {
            inputListener.updateMousePos(camera);
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
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouse);
        return new Point((int) Math.floor(mouse.x / 8f), (int) Math.floor(mouse.y / 8f));
    }
}
