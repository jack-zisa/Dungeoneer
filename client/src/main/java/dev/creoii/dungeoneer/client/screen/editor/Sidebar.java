package dev.creoii.dungeoneer.client.screen.editor;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
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
import dev.creoii.dungeoneer.client.editor.selection.AreaSelection;
import dev.creoii.dungeoneer.client.editor.selection.Selection;
import dev.creoii.dungeoneer.client.screen.AbstractScreen;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.provider.tileprovider.SimpleTileProvider;
import dev.creoii.dungeoneer.util.provider.tileprovider.TileProvider;

import java.util.Random;

public class Sidebar extends Table {
    protected static final NinePatchDrawable TAB_BACKGROUND = new NinePatchDrawable(Assets.TAB_9PATCH);
    private final DungeonEditorScreen screen;
    private int brushSize;
    private String selectedLayer;
    private TileProvider selectedTile;
    private Selection selection;

    public Sidebar(DungeonEditorScreen screen) {
        super(AbstractScreen.SKIN);
        this.screen = screen;
        brushSize = 1;
        selectedLayer = Constants.MAP_LAYER_GROUND;

        Table layerTable = new Table();
        layerTable.add(new Label("Layer", getSkin()));

        ButtonGroup<TextButton> layers = new ButtonGroup<>();
        layers.setMinCheckCount(1);
        layers.setMaxCheckCount(1);
        layers.setUncheckLast(true);

        TextButton groundButton = new TextButton(Constants.MAP_LAYER_GROUND, getSkin());
        groundButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedLayer = Constants.MAP_LAYER_GROUND;
            }
        });
        layers.add(groundButton);
        layerTable.add(groundButton);

        TextButton wallButton = new TextButton(Constants.MAP_LAYER_WALL, getSkin());
        wallButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedLayer = Constants.MAP_LAYER_WALL;
            }
        });
        layers.add(wallButton);
        layerTable.add(wallButton);
        add(layerTable).grow().row();

        Table brushTable = new Table();
        brushTable.add(new Label("Brush Size", getSkin()));
        Slider brushSize = new Slider(1, 8, 1, false, getSkin());
        brushSize.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setBrushSize((int) ((Slider) actor).getValue());
            }
        });
        brushTable.add(brushSize).padLeft(10f).width(100f);
        add(brushTable).grow().row();

        add(new Label("Selections", getSkin())).top().center().row();
        Table selectionTable = new Table();
        ButtonGroup<TextButton> selections = new ButtonGroup<>();
        selections.setMinCheckCount(0);
        selections.setMaxCheckCount(1);
        selections.setUncheckLast(true);

        TextButton area = addSelectionButton(selectionTable, selections, "Area", new AreaSelection());
        add(selectionTable).grow().row();

        add(new Label("Tools", getSkin())).top().center().row();
        Table toolsTable = new Table();
        ButtonGroup<TextButton> tools = new ButtonGroup<>();
        tools.setMinCheckCount(0);
        tools.setMaxCheckCount(1);
        tools.setUncheckLast(true);

        TextButton fillButton = addToolButton(toolsTable, tools, "Fill", () -> {
            if (selection != null && selectedTile != null) {
                TiledMapTileLayer tileLayer = (TiledMapTileLayer) screen.getMapRenderer().getMap().getLayers().get(selectedLayer);
                selection.forEach(tileLayer, cell -> {
                    if (cell != null) cell.setTile(ClientTiles.getTile(selectedTile.get(new Random()).id()));
                });
            }
        });
        TextButton deleteButton = addToolButton(toolsTable, tools, "Delete", () -> {
            if (selection != null) {
                selection.forEach((TiledMapTileLayer) screen.getMapRenderer().getMap().getLayers().get(selectedLayer), cell -> {
                    if (cell != null) cell.setTile(null);
                });
            }
        });
        add(toolsTable).grow().row();

        add(new Label("Tiles", getSkin())).center().row();
        Table tilesTable = new Table();
        ButtonGroup<ImageButton> tiles = new ButtonGroup<>();
        tiles.setMinCheckCount(0);
        tiles.setMaxCheckCount(1);
        tiles.setUncheckLast(true);

        int index = 0;

        for (TiledMapTile tile : ClientTiles.TILES.values()) {
            addTileButton(tilesTable, tiles, tile.getTextureRegion(), tile);
            if (++index % 6 == 0) {
                tilesTable.row();
            }
        }
        add(tilesTable).grow().row();

        add(new Label("Tile Providers", getSkin())).center().row();
        Table tileProvidersTable = new Table();
        ButtonGroup<ImageButton> tileProviders = new ButtonGroup<>();
        tileProviders.setMinCheckCount(0);
        tileProviders.setMaxCheckCount(1);
        tileProviders.setUncheckLast(true);

        for (Identifiable identifiable : DataManager.getTileProviders().values()) {
            TileProvider tileProvider = (TileProvider) identifiable;
            addTileProviderButton(tileProvidersTable, tileProviders, ClientTiles.getTile(tileProvider.getTile().id()).getTextureRegion(), tileProvider);
            if (++index % 6 == 0) {
                tileProvidersTable.row();
            }
        }
        add(tileProvidersTable).grow();

        setBackground(TAB_BACKGROUND);
    }

    public String getSelectedLayer() {
        return selectedLayer;
    }

    public int getBrushSize() {
        return brushSize;
    }

    public void setBrushSize(int brushSize) {
        this.brushSize = brushSize;
    }

    public TileProvider getSelectedTile() {
        return selectedTile;
    }

    public Selection getSelection() {
        return selection;
    }

    private TextButton addSelectionButton(Table table, ButtonGroup<TextButton> group, String label, Selection newSelection) {
        TextButton button = new TextButton(label, getSkin());
        group.add(button);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (button.isChecked()) {
                    selection = newSelection;
                    newSelection.clear();
                } else selection = null;
                screen.getInputListener().setSelecting(false);
            }
        });
        table.add(button);
        return button;
    }

    private TextButton addToolButton(Table table, ButtonGroup<TextButton> group, String label, Runnable runnable) {
        TextButton button = new TextButton(label, getSkin());
        group.add(button);
        button.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                runnable.run();
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        table.add(button);
        return button;
    }

    private ImageButton addTileButton(Table table, ButtonGroup<ImageButton> group, TextureRegion texture, TiledMapTile tile) {
        ImageButton button = new ImageButton(new TextureRegionDrawable(texture));
        group.add(button);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String id = ClientTiles.TILES.inverse().get(tile);
                selectedTile = button.isChecked() ? new SimpleTileProvider(id, DataManager.getTile(id)) : null;
            }
        });
        table.add(button);
        return button;
    }

    private ImageButton addTileProviderButton(Table table, ButtonGroup<ImageButton> group, TextureRegion texture, TileProvider tileProvider) {
        ImageButton button = new ImageButton(new TextureRegionDrawable(texture));
        group.add(button);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectedTile = button.isChecked() ? tileProvider : null;
            }
        });
        table.add(button);
        return button;
    }
}
