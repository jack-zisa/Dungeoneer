package dev.creoii.dungeoneer.client.render.screen.editor;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.game.ClientDungeonMap;
import dev.creoii.dungeoneer.client.render.screen.AbstractScreen;
import dev.creoii.dungeoneer.definitions.map.tile.Tileset;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.Identifiable;

public class Sidebar2 extends Table {
    protected static final NinePatchDrawable TAB_BACKGROUND = new NinePatchDrawable(Assets.TAB_9PATCH);
    private final DungeonEditorScreen2 screen;
    private String selectedLayer;
    private final Table tilesetsTable;
    private final ButtonGroup<ImageButton> tilesets;

    public Sidebar2(DungeonEditorScreen2 screen) {
        super(AbstractScreen.SKIN);
        this.screen = screen;
        selectedLayer = Constants.MAP_LAYER_GROUND;

        Table layerTable = new Table();
        layerTable.add(new Label("Layer", getSkin())).row();

        ButtonGroup<TextButton> layers = new ButtonGroup<>();
        layers.setMinCheckCount(1);
        layers.setMaxCheckCount(1);
        layers.setUncheckLast(true);

        TextButton groundButton = new TextButton(Constants.MAP_LAYER_GROUND, getSkin());
        groundButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedLayer = Constants.MAP_LAYER_GROUND;
                screen.getSelectedLayerLabel().setText(selectedLayer);
                refreshTilesTable();
            }
        });
        layers.add(groundButton);
        layerTable.add(groundButton);

        TextButton objectButton = new TextButton(Constants.MAP_LAYER_OBJECT, getSkin());
        objectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedLayer = Constants.MAP_LAYER_OBJECT;
                screen.getSelectedLayerLabel().setText(selectedLayer);
                refreshTilesTable();
            }
        });
        layers.add(objectButton);
        layerTable.add(objectButton).row();

        TextButton wallButton = new TextButton(Constants.MAP_LAYER_WALL, getSkin());
        wallButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedLayer = Constants.MAP_LAYER_WALL;
                screen.getSelectedLayerLabel().setText(selectedLayer);
                refreshTilesTable();
            }
        });
        layers.add(wallButton);
        layerTable.add(wallButton);
        add(layerTable).grow().row();

        add(new Label("Map Templates", getSkin())).top().center().row();
        SelectBox<String> templateSelectBox = new SelectBox<>(getSkin());
        templateSelectBox.setItems(DataManager.getMapTemplates().values().stream().map(Identifiable::id).toArray(String[]::new));
        templateSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String templateId = templateSelectBox.getSelected();
                ClientDungeonMap dungeon = screen.getClient().getState().getDungeonMap();
                if (dungeon != null) {
                    screen.getClient().getState().setDungeonMap(dungeon.get().withTemplateId(templateId));
                }
            }
        });
        add(templateSelectBox).width(200).row();

        add(new Label("Tilesets", getSkin())).center().row();
        tilesetsTable = new Table();
        tilesets = new ButtonGroup<>();
        tilesets.setMinCheckCount(0);
        tilesets.setMaxCheckCount(1);
        tilesets.setUncheckLast(true);

        refreshTilesTable();
        add(tilesetsTable).grow();

        setBackground(TAB_BACKGROUND);
    }

    public String getSelectedLayer() {
        return selectedLayer;
    }

    public TiledMapTileLayer getActiveLayer() {
        return (TiledMapTileLayer) screen.getClient().getState().getDungeonMap().getMap().getLayers().get(selectedLayer);
    }

    private void addTilesetButton(Table table, ButtonGroup<ImageButton> group, TextureRegion texture, Tileset tileset) {
        TextureRegionDrawable drawable = new TextureRegionDrawable(texture);
        drawable.setMinSize(24f, 24f);
        ImageButton button = new ImageButton(drawable);
        button.setName(tileset.id());
        group.add(button);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (button.isChecked()) {
                    button.getImage().setColor(0.7f, 0.7f, 0.7f, 1f);
                    button.getImage().setScale(1.1f);

                    String tilesetId = button.getName();
                    ClientDungeonMap dungeon = screen.getClient().getState().getDungeonMap();
                    if (dungeon != null) {
                        screen.getClient().getState().setDungeonMap(dungeon.get().withTilesetId(tilesetId));
                    }
                }
            }
        });
        table.add(button).pad(2f);
    }

    private void refreshTilesTable() {
        tilesetsTable.clearChildren();
        int index = 0;
        for (Identifiable identifiable : DataManager.getTilesets().values()) {
            Tileset tileset = (Tileset) identifiable;
            addTilesetButton(tilesetsTable, tilesets, ClientTiles.getTile(tileset.ground().id()).getTextureRegion(), tileset);
            if (++index % 6 == 0) {
                tilesetsTable.row();
            }
        }
    }
}
