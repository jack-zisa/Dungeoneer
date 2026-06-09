package dev.creoii.dungeoneer.client.screen.editor;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import dev.creoii.dungeoneer.client.AssetManager;
import dev.creoii.dungeoneer.client.screen.AbstractScreen;

public class Sidebar extends Table {
    protected static final NinePatchDrawable TAB_BACKGROUND = new NinePatchDrawable(AssetManager.TAB_9PATCH);
    private TiledMapTile selectedTile;

    public Sidebar() {
        super(AbstractScreen.SKIN);
        add(new Label("Toolbar", getSkin())).top().center().row();

        add(new Table()).grow().row();

        add(new Label("Tiles", getSkin())).center().row();

        Table tilesTable = new Table();
        ButtonGroup<ImageButton> tiles = new ButtonGroup<>();
        tiles.setMinCheckCount(0);
        tiles.setMaxCheckCount(1);
        tiles.setUncheckLast(true);

        ImageButton dirt = addTileButton(tilesTable, tiles, AssetManager.DIRT_TEXTURE, Tiles.DIRT);
        ImageButton grass = addTileButton(tilesTable, tiles, AssetManager.GRASS_TEXTURE, Tiles.GRASS);
        ImageButton lava = addTileButton(tilesTable, tiles, AssetManager.LAVA_TEXTURE, Tiles.LAVA);
        ImageButton sand = addTileButton(tilesTable, tiles, AssetManager.SAND_TEXTURE, Tiles.SAND);
        ImageButton stone = addTileButton(tilesTable, tiles, AssetManager.STONE_TEXTURE, Tiles.STONE);
        add(tilesTable).grow();

        setBackground(TAB_BACKGROUND);
    }

    public TiledMapTile getSelectedTile() {
        return selectedTile;
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
