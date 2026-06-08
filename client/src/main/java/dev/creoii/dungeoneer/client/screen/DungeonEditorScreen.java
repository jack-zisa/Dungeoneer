package dev.creoii.dungeoneer.client.screen;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
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

public class DungeonEditorScreen extends AbstractScreen {
    private final Dungeoneer client;
    private OrthographicCamera camera;
    private OrthogonalTiledMapRenderer mapRenderer;

    public DungeonEditorScreen(Dungeoneer client) {
        this.client = client;
    }

    public Dungeoneer getClient() {
        return client;
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
        camera.zoom = .4f;

        mapRenderer = new OrthogonalTiledMapRenderer(buildTestMap());

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
        getStage().addListener(new DungeonEditorInputListener(client, camera));
        super.show();
    }

    @Override
    public void render(float delta) {
        mapRenderer.setView(camera);
        mapRenderer.render();

        super.render(delta);
    }

    @Override
    public void dispose() {
        super.dispose();
        SKIN.dispose();
    }

    public static class DungeonEditor extends Table {
        public DungeonEditor() {
        }
    }
}
