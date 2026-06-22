package dev.creoii.dungeoneer.client.render.screen.editor;

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
import com.mojang.datafixers.util.Either;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.editor.selection.AreaSelection;
import dev.creoii.dungeoneer.client.editor.selection.Selection;
import dev.creoii.dungeoneer.client.render.screen.AbstractScreen;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.provider.Provider;
import dev.creoii.dungeoneer.util.provider.mapobjectprovider.MapObjectProvider;
import dev.creoii.dungeoneer.util.provider.mapobjectprovider.SimpleMapObjectProvider;
import dev.creoii.dungeoneer.util.provider.tileprovider.SimpleTileProvider;
import dev.creoii.dungeoneer.util.provider.tileprovider.TileProvider;

import java.util.Random;

public class Sidebar extends Table {
    protected static final NinePatchDrawable TAB_BACKGROUND = new NinePatchDrawable(Assets.TAB_9PATCH);
    private final DungeonEditorScreen screen;
    private int brushSize;
    private String selectedLayer;
    private final Table tilesTable;
    private final ButtonGroup<ImageButton> tileProviders;
    private Either<TileProvider, MapObjectProvider> selectedTile;
    private Selection selection;

    public Sidebar(DungeonEditorScreen screen) {
        super(AbstractScreen.SKIN);
        this.screen = screen;
        brushSize = 1;
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
                refreshTilesTable();
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
                    if (cell != null) {
                        if (selectedTile.left().isPresent()) {
                            TileProvider provider = selectedTile.left().get();
                            cell.setTile(ClientTiles.getTile(provider.get(new Random()).id()));
                        } else if (selectedTile.right().isPresent()) {
                            MapObjectProvider provider = selectedTile.right().get();
                            cell.setTile(ClientTiles.getObject(provider.get(new Random()).id()));
                        }
                    }
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
        tilesTable = new Table();
        tileProviders = new ButtonGroup<>();
        tileProviders.setMinCheckCount(0);
        tileProviders.setMaxCheckCount(1);
        tileProviders.setUncheckLast(true);

        refreshTilesTable();

        add(tilesTable).grow();

        setBackground(TAB_BACKGROUND);
    }

    public String getSelectedLayer() {
        return selectedLayer;
    }

    public TiledMapTileLayer getActiveLayer() {
        return (TiledMapTileLayer) screen.getMapRenderer().getMap().getLayers().get(selectedLayer);
    }

    public int getBrushSize() {
        return brushSize;
    }

    public void setBrushSize(int brushSize) {
        this.brushSize = brushSize;
    }

    public Either<TileProvider, MapObjectProvider> getSelectedTile() {
        return selectedTile;
    }

    public Selection getSelection() {
        return selection;
    }

    public void setSelectedTile(Provider<?> provider) {
        selectedTile = switch (provider) {
            case null -> null;
            case TileProvider tileProvider -> Either.left(tileProvider);
            case MapObjectProvider mapObjectProvider -> Either.right(mapObjectProvider);
            default -> throw new IllegalStateException();
        };

        String selectedId = switch (provider) {
            case null -> null;
            case TileProvider tileProvider -> tileProvider.getTile().id();
            case MapObjectProvider mapObjectProvider -> mapObjectProvider.getMapObject().id();
            default -> null;
        };

        for (Actor actor : tilesTable.getChildren()) {
            if (actor instanceof ImageButton button) {
                boolean selected = selectedId != null && selectedId.equals(button.getName());
                button.getImage().setColor(selected ? .7f : 1f, selected ? .7f : 1f, selected ? .7f : 1f, 1f);
                button.getImage().setScale(selected ? 1.1f : 1f);
            }
        }
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

    private void addTileProviderButton(Table table, ButtonGroup<ImageButton> group, TextureRegion texture, Provider<?> provider) {
        TextureRegionDrawable drawable = new TextureRegionDrawable(texture);
        drawable.setMinSize(24f, 24f);
        ImageButton button = new ImageButton(drawable);
        button.setName(provider instanceof TileProvider tileProvider ? tileProvider.getTile().id() : ((MapObjectProvider) provider).getMapObject().id());
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

                setSelectedTile(button.isChecked() ? provider : null);
            }
        });
        table.add(button).pad(2f);
    }

    private void refreshTilesTable() {
        tilesTable.clearChildren();

        int index = 0;
        if (!selectedLayer.equals(Constants.MAP_LAYER_OBJECT)) {
            for (TiledMapTile tile : ClientTiles.TILES.values()) {
                String tileId = ClientTiles.getTileId(tile);
                TileProvider tileProvider = new SimpleTileProvider(tileId, DataManager.getTile(tileId));
                addTileProviderButton(tilesTable, tileProviders, tile.getTextureRegion(), tileProvider);
                if (++index % 6 == 0) {
                    tilesTable.row();
                }
            }

            tilesTable.row();

            for (Identifiable identifiable : DataManager.getTileProviders().values()) {
                TileProvider tileProvider = (TileProvider) identifiable;
                addTileProviderButton(tilesTable, tileProviders, ClientTiles.getTile(tileProvider.getTile().id()).getTextureRegion(), tileProvider);
                if (++index % 6 == 0) {
                    tilesTable.row();
                }
            }
        } else {
            for (TiledMapTile tile : ClientTiles.OBJECTS.values()) {
                String tileId = ClientTiles.getObjectId(tile);
                MapObjectProvider mapObjectProvider = new SimpleMapObjectProvider(tileId, DataManager.getMapObject(tileId));
                addTileProviderButton(tilesTable, tileProviders, tile.getTextureRegion(), mapObjectProvider);
                if (++index % 6 == 0) {
                    tilesTable.row();
                }
            }
        }
    }
}
