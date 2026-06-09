package dev.creoii.dungeoneer.client.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.control.DungeonEditorInputListener;
import dev.creoii.dungeoneer.client.screen.main.MainScreen;
import dev.creoii.dungeoneer.definitions.DungeonMap;
import dev.creoii.dungeoneer.network.c2s.dungeon.SaveDungeonMapC2S;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class DungeonEditorScreen extends AbstractScreen {
    private final Dungeoneer client;
    private DungeonEditor editor;
    private DungeonEditorInputListener inputListener;
    private OrthographicCamera camera;
    private OrthogonalTiledMapRenderer mapRenderer;
    private ShapeRenderer shapeRenderer;

    private Label hoverPosLabel;

    public DungeonEditorScreen(Dungeoneer client) {
        this.client = client;
    }

    public Dungeoneer getClient() {
        return client;
    }

    public DungeonEditor getEditor() {
        return editor;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public OrthogonalTiledMapRenderer getMapRenderer() {
        return mapRenderer;
    }

    public void setMapRenderer(OrthogonalTiledMapRenderer mapRenderer) {
        this.mapRenderer = mapRenderer;
        editor.init(mapRenderer.getMap());
    }

    public TiledMap buildEmptyMap() {
        TiledMap map = new TiledMap();
        TiledMapTileLayer ground = new TiledMapTileLayer(256, 256, 8, 8);
        ground.setName("ground");
        map.getLayers().add(ground);
        return map;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.zoom = 1f;

        inputListener = new DungeonEditorInputListener(this);

        editor = new DungeonEditor(client);
        setMapRenderer(new OrthogonalTiledMapRenderer(client.getState().getDungeonMap() == null ? buildEmptyMap() : editor.read(client.getState().getDungeonMap().mapData())));

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);

        Table root = new Table();
        root.setFillParent(true);

        Label title = new Label("Editing Dungeon", SKIN);
        root.add(title).top().left().padBottom(30).row();
        hoverPosLabel = new Label("", SKIN);
        root.add(hoverPosLabel).left().row();

        root.add(editor).grow().row();

        TextButton saveButton = new TextButton("Save", SKIN);
        saveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                editor.save();
            }
        });
        root.add(saveButton).bottom();

        TextButton finishButton = new TextButton("Finish", SKIN);
        finishButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                client.setScreen(new MainScreen(client));
                client.getState().setStatus(ClientState.Status.LOBBY);
            }
        });
        root.add(finishButton).bottom();

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
        shapeRenderer.rect(0f, 0f, 256f * 8f, 256f * 8f);
        Point hover = getHoveredPos();
        if (hover.getX() >= 0f && hover.getY() >= 0f && hover.getX() < 256f && hover.getY() < 256f) {
            shapeRenderer.setColor(0f, 1f, 1f, 1f);
            shapeRenderer.rect(hover.x * 8f, hover.y * 8f, 8f, 8f);
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
        return new Point((int)(mouse.x / 8f), (int)(mouse.y / 8f));
    }

    @Nullable
    public TiledMapTileLayer.Cell getCellAt(Point point) {
        TiledMapTileLayer layer = (TiledMapTileLayer) mapRenderer.getMap().getLayers().get("ground");
        return layer.getCell(point.x, point.y);
    }

    public static class DungeonEditor extends Table {
        private final Dungeoneer client;
        private TiledMap map;

        public DungeonEditor(Dungeoneer client) {
            this.client = client;
        }

        public void init(TiledMap map) {
            this.map = map;
        }

        public void save() {
            try {
                byte[] blob = serializeMap(map);
                client.getState().setDungeonMap(new DungeonMap(-1L, client.getState().getAccount().id(), blob, LocalDateTime.now()));
                client.get().sendTCP(new SaveDungeonMapC2S(client.getState().getAccount().id(), blob));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public TiledMap read(byte[] blob) {
            try {
                TiledMapTileSet tiledMapTileSet = new TiledMapTileSet();
                tiledMapTileSet.putTile(1, DungeonEditorInputListener.TILE);
                return deserializeMap(blob, tiledMapTileSet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public byte[] serializeMap(TiledMap map) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (DataOutputStream dos = new DataOutputStream(baos)) {
                if (map.getLayers().get("ground") instanceof TiledMapTileLayer tileLayer) {
                    byte[] layerBlob = serializeLayer(tileLayer);
                    dos.writeInt(layerBlob.length);
                    dos.write(layerBlob);
                }
            }
            return baos.toByteArray();
        }

        public byte[] serializeLayer(TiledMapTileLayer layer) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (DataOutputStream dos = new DataOutputStream(new DeflaterOutputStream(baos))) {
                dos.writeInt(layer.getWidth());
                dos.writeInt(layer.getHeight());
                for (int y = 0; y < layer.getHeight(); ++y) {
                    for (int x = 0; x < layer.getWidth(); ++x) {
                        TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                        int tileId = cell == null || cell.getTile() == null ? 0 : cell.getTile().getId();
                        dos.writeInt(tileId);
                    }
                }
            }
            return baos.toByteArray();
        }

        public TiledMap deserializeMap(byte[] blob, TiledMapTileSet tileSet) throws IOException {
            TiledMap map = new TiledMap();
            try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(blob))) {
                byte[] layerBlob = new byte[dis.readInt()];
                dis.readFully(layerBlob);
                TiledMapTileLayer layer = deserializeLayer(layerBlob, tileSet);
                layer.setName("ground");
                map.getLayers().add(layer);
            }

            map.getTileSets().addTileSet(tileSet);
            return map;
        }

        public TiledMapTileLayer deserializeLayer(byte[] blob, TiledMapTileSet tileSet) throws IOException {
            try (DataInputStream dis = new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(blob)))) {
                int width = dis.readInt();
                int height = dis.readInt();
                TiledMapTileLayer layer = new TiledMapTileLayer(width, height, 8, 8);
                for (int y = 0; y < height; ++y) {
                    for (int x = 0; x < width; ++x) {
                        int tileId;
                        if ((tileId = dis.readInt()) == 0)
                            continue;

                        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                        cell.setTile(tileSet.getTile(tileId));
                        layer.setCell(x, y, cell);
                    }
                }
                return layer;
            }
        }
    }
}
