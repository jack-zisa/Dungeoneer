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
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.editor.selection.AreaSelection;
import dev.creoii.dungeoneer.client.editor.selection.Selection;
import dev.creoii.dungeoneer.client.screen.AbstractScreen;

public class Sidebar extends Table {
    protected static final NinePatchDrawable TAB_BACKGROUND = new NinePatchDrawable(Assets.TAB_9PATCH);
    private final DungeonEditorScreen screen;
    private TiledMapTile selectedTile;
    private Selection selection;

    public Sidebar(DungeonEditorScreen screen) {
        super(AbstractScreen.SKIN);
        this.screen = screen;
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
                TiledMapTileLayer tileLayer = (TiledMapTileLayer) screen.getMapRenderer().getMap().getLayers().get("ground");
                selection.forEach(tileLayer, cell -> {
                    if (cell != null) cell.setTile(selectedTile);
                });
            }
        });
        TextButton deleteButton = addToolButton(toolsTable, tools, "Delete", () -> {
            if (selection != null) {
                selection.forEach((TiledMapTileLayer) screen.getMapRenderer().getMap().getLayers().get("ground"), cell -> {
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

        for (TiledMapTile tile : Tiles.TILES.values()) {
            addTileButton(tilesTable, tiles, tile.getTextureRegion(), tile);
            if (++index % 6 == 0) {
                tilesTable.row();
            }
        }
        add(tilesTable).grow();

        setBackground(TAB_BACKGROUND);
    }

    public TiledMapTile getSelectedTile() {
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
                selectedTile = button.isChecked() ? tile : null;
            }
        });
        table.add(button);
        return button;
    }
}
