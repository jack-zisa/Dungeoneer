package dev.creoii.dungeoneer.client.screen.main;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import dev.creoii.dungeoneer.client.AssetManager;
import dev.creoii.dungeoneer.client.Dungeoneer;

public abstract class Tab extends Table {
    protected static final NinePatchDrawable TAB_BACKGROUND = new NinePatchDrawable(AssetManager.TAB_9PATCH);
    protected static final NinePatchDrawable TAB_SELECTED_BACKGROUND = new NinePatchDrawable(AssetManager.TAB_SELECTED_9PATCH);
    private final Dungeoneer client;
    private final ImageButton tabButton;

    protected Tab(Dungeoneer client, TextureRegion tabTexture) {
        super(MainScreen.SKIN);
        this.client = client;

        setFillParent(true);
        init();
        build();

        Image leftArrow = new Image(new TextureRegionDrawable(AssetManager.TAB_ARROW_TEXTURE));
        Image rightArrow = new Image(new TextureRegionDrawable(AssetManager.TAB_ARROW_TEXTURE));
        rightArrow.setScaleX(-1f); // TODO: Replace with TextureRegion#flip

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = TAB_BACKGROUND;
        style.checked = TAB_SELECTED_BACKGROUND;
        style.imageUp = new TextureRegionDrawable(tabTexture);
        tabButton = new ImageButton(style);
        tabButton.getImageCell().size(48f, 48f);

        Image image = tabButton.getImage();
        tabButton.clearChildren();

        tabButton.add(leftArrow).size(16f);
        tabButton.add(image).size(48f);
        tabButton.add(rightArrow).padLeft(15f).size(16f);

        TiledDrawable background = new TiledDrawable(AssetManager.BACKGROUND_BRICK_TEXTURE);
        background.setScale(3f);
        setBackground(background);
    }

    public Dungeoneer getClient() {
        return client;
    }

    public ImageButton getTabButton() {
        return tabButton;
    }

    public void init() {
    }

    public void select() {
    }

    protected abstract void build();
}
