package dev.creoii.dungeoneer.client.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import dev.creoii.dungeoneer.client.AssetManager;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.control.DungeonEditorInputListener;
import dev.creoii.dungeoneer.client.screen.main.MainScreen;

import javax.annotation.Nullable;
import java.awt.*;

public class DungeonEditorScreen extends AbstractScreen {
    private final Dungeoneer client;
    private OrthographicCamera camera;
    private OrthogonalTiledMapRenderer mapRenderer;
    private ShapeRenderer shapeRenderer;

    public DungeonEditorScreen(Dungeoneer client) {
        this.client = client;
    }

    public Dungeoneer getClient() {
        return client;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public TiledMap buildTestMap() {
        TiledMap map = new TiledMap();
        TiledMapTileLayer ground = new TiledMapTileLayer(64, 64, 8, 8);
        ground.setName("ground");

        StaticTiledMapTile tile = new StaticTiledMapTile(AssetManager.STONE_TEXTURE);
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(tile);
                ground.setCell(x, y, cell);
            }
        }

        map.getLayers().add(ground);
        return map;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.zoom = 1f;

        mapRenderer = new OrthogonalTiledMapRenderer(buildTestMap());
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);

        Table root = new Table();
        root.setFillParent(true);

        TiledDrawable uiBackground = new TiledDrawable(AssetManager.BACKGROUND_BRICK_TEXTURE);
        uiBackground.setScale(3f);

        Label title = new Label("Editing Dungeon", SKIN);
        root.add(title).top().padBottom(30).row();

        final DungeonEditor editor = new DungeonEditor();
        root.add(editor).grow().row();

        TextButton cancelButton = new TextButton("Cancel", SKIN);
        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                client.setScreen(new MainScreen(client));
                client.getState().setStatus(ClientState.Status.LOBBY);
            }
        });
        cancelButton.setBackground(uiBackground);
        root.add(cancelButton).bottom();

        getStage().addActor(root);
        getStage().addListener(new DungeonEditorInputListener(this));
        super.show();
    }

    @Override
    public void render(float delta) {
        mapRenderer.setView(camera);
        mapRenderer.render();

        Point hover = getHoveredPos();
        TiledMapTileLayer.Cell cell = getCellAt(hover);
        if (cell != null) {
            shapeRenderer.setProjectionMatrix(camera.combined);

            shapeRenderer.begin();
            shapeRenderer.rect(hover.x * 8f, hover.y * 8f, 8f, 8f);
            shapeRenderer.end();
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

        int tileX = (int)(mouse.x / 8f);
        int tileY = (int)(mouse.y / 8f);

        return new Point(tileX, tileY);
    }

    @Nullable
    public TiledMapTileLayer.Cell getCellAt(Point point) {
        TiledMapTileLayer layer = (TiledMapTileLayer) mapRenderer.getMap().getLayers().get("ground");
        return layer.getCell(point.x, point.y);
    }

    public static class DungeonEditor extends Table {
        public DungeonEditor() {
        }
    }
}
