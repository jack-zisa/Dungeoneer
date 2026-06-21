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
import com.badlogic.gdx.utils.Scaling;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.editor.selection.AreaSelection;
import dev.creoii.dungeoneer.client.editor.selection.Selection;
import dev.creoii.dungeoneer.client.screen.AbstractScreen;
import dev.creoii.dungeoneer.definitions.Tile;
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
        Table tileProvidersTable = new Table();
        ButtonGroup<ImageButton> tileProviders = new ButtonGroup<>();
        tileProviders.setMinCheckCount(0);
        tileProviders.setMaxCheckCount(1);
        tileProviders.setUncheckLast(true);

        int index = 0;
        for (TiledMapTile tile : ClientTiles.TILES.values()) {
            String tileId = ClientTiles.getTileId(tile);
            TileProvider tileProvider = new SimpleTileProvider(tileId, DataManager.getTile(tileId));
            addTileProviderButton(tileProvidersTable, tileProviders, tile.getTextureRegion(), tileProvider);
            if (++index % 6 == 0) {
                tileProvidersTable.row();
            }
        }

        tileProvidersTable.row();

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

    public void setSelectedTile(TileProvider selectedTile) {
        this.selectedTile = selectedTile;
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

    private void addTileProviderButton(Table table, ButtonGroup<ImageButton> group, TextureRegion texture, TileProvider tileProvider) {
        TextureRegionDrawable drawable = new TextureRegionDrawable(texture);
        drawable.setMinSize(24f, 24f);
        ImageButton button = new ImageButton(drawable);
        group.add(button);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (button.isChecked()) {
                    button.getImage().setColor(0.7f, 0.7f, 0.7f, 1f);
                    button.getImage().setScale(1.1f);
                } else {
                    button.getImage().setColor(1f, 1f, 1f, 1f);
                    button.getImage().setScale(1f);
                }

                setSelectedTile(button.isChecked() ? tileProvider : null);
            }
        });
        table.add(button).pad(2f);
    }
}
