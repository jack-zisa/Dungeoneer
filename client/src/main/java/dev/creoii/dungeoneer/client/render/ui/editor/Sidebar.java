package dev.creoii.dungeoneer.client.render.ui.editor;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.game.ClientDungeonMap;
import dev.creoii.dungeoneer.client.render.ui.screen.AbstractScreen;
import dev.creoii.dungeoneer.definitions.map.tile.Tileset;
import dev.creoii.dungeoneer.util.Identifiable;

public class Sidebar extends Table {
    protected static final NinePatchDrawable TAB_BACKGROUND = new NinePatchDrawable(Assets.TAB_9PATCH);
    private final DungeonEditorScreen screen;
    private final Table tilesetsTable;
    private final ButtonGroup<ImageButton> tilesets;

    public Sidebar(DungeonEditorScreen screen) {
        super(AbstractScreen.SKIN);
        this.screen = screen;

        Table layerTable = new Table();
        layerTable.add(new Label("Layer", AbstractScreen.SKIN)).row();

        ButtonGroup<TextButton> layers = new ButtonGroup<>();
        layers.setMinCheckCount(1);
        layers.setMaxCheckCount(1);
        layers.setUncheckLast(true);

        add(new Label("Map Templates", AbstractScreen.SKIN)).top().center().row();
        SelectBox<String> templateSelectBox = new SelectBox<>(AbstractScreen.SKIN);
        templateSelectBox.setItems(DataManager.getMapTemplates().values().stream().map(Identifiable::id).toArray(String[]::new));
        templateSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String templateId = templateSelectBox.getSelected();
                ClientDungeonMap dungeon = screen.getClient().getState().getEditorDungeonMap();
                if (dungeon != null) {
                    screen.getClient().getState().setDungeonMap(dungeon.get().withTemplateId(templateId));
                }
            }
        });
        add(templateSelectBox).width(200).row();

        add(new Label("Tilesets", AbstractScreen.SKIN)).center().row();
        tilesetsTable = new Table();
        tilesets = new ButtonGroup<>();
        tilesets.setMinCheckCount(0);
        tilesets.setMaxCheckCount(1);
        tilesets.setUncheckLast(true);

        refreshTilesTable();
        add(tilesetsTable).grow();

        setBackground(TAB_BACKGROUND);
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
                    ClientDungeonMap dungeon = screen.getClient().getState().getEditorDungeonMap();
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
