package dev.creoii.dungeoneer.client.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

public class MainScreen extends AbstractScreen {
    private final TextureRegion marketTexture = new TextureRegion(new Texture("textures/ui/market.png"));
    private final TextureRegion throneTexture = new TextureRegion(new Texture("textures/ui/throne.png"));
    private final TextureRegion chestTexture = new TextureRegion(new Texture("textures/ui/chest.png"));
    private final TextureRegion towerTexture = new TextureRegion(new Texture("textures/ui/tower.png"));
    private final TextureRegion skullTexture = new TextureRegion(new Texture("textures/ui/skull.png"));
    private final NinePatch tab = new NinePatch(new Texture("textures/ui/tab.png"), 2, 2, 2 ,2);
    private final NinePatch tabSelected = new NinePatch(new Texture("textures/ui/tab_selected.png"), 2, 2, 2 ,2);

    private Skin skin;
    private Actor selected;

    private Table tabBar;
    private float tabWidth;
    private float tabSelectedWidth;
    private final Array<ImageButton> tabButtons;

    public MainScreen() {
        tabButtons = new Array<>(5);

        tab.scale(4f, 4f);
        tabSelected.scale(4f, 4f);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        float stageWidth = getStage().getWidth();
        tabWidth = stageWidth / 6f;
        tabSelectedWidth = stageWidth / 3f;

        tabBar.clearChildren();

        for (ImageButton button : tabButtons) {
            tabBar.add(button).width(button == selected ? tabSelectedWidth : tabWidth).growY();
        }
    }

    @Override
    public void show() {
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        float stageWidth = getStage().getWidth();
        tabWidth = stageWidth / 6f;
        tabSelectedWidth = stageWidth / 3f;

        final NinePatchDrawable tabBackground = new NinePatchDrawable(tab);
        final NinePatchDrawable tabSelectedBackground = new NinePatchDrawable(tabSelected);

        Table root = new Table();
        root.setFillParent(true);

        Stack content = new Stack();
        final ShopTab shopTab = new ShopTab(skin, tabBackground, tabSelectedBackground, marketTexture);
        final VaultThroneTab vaultThroneTab = new VaultThroneTab(skin, tabBackground, tabSelectedBackground, throneTexture);
        final PlayTab playTab = new PlayTab(skin, tabBackground, tabSelectedBackground, chestTexture);
        final FactionTab factionTab = new FactionTab(skin, tabBackground, tabSelectedBackground, towerTexture);
        final DungeonGamesTab dungeonGamesTab = new DungeonGamesTab(skin, tabBackground, tabSelectedBackground, skullTexture);
        content.addActor(shopTab);
        content.addActor(vaultThroneTab);
        content.addActor(playTab);
        content.addActor(factionTab);
        content.addActor(dungeonGamesTab);
        root.add(content).expand().fill().row();

        tabBar = new Table();
        root.add(tabBar).growX().height(60);
        tabBar.defaults().expandX().fillX().height(60);
        tabBar.add(shopTab.getTabButton()).expandX().fillX();
        tabBar.add(vaultThroneTab.getTabButton()).expandX().fillX();
        tabBar.add(playTab.getTabButton()).expandX().fillX();
        tabBar.add(factionTab.getTabButton()).expandX().fillX();
        tabBar.add(dungeonGamesTab.getTabButton()).expandX().fillX();
        selected = playTab.getTabButton();

        ChangeListener tabListener = new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selected = actor;

                shopTab.setVisible(shopTab.getTabButton().isChecked());
                vaultThroneTab.setVisible(vaultThroneTab.getTabButton().isChecked());
                playTab.setVisible(playTab.getTabButton().isChecked());
                factionTab.setVisible(factionTab.getTabButton().isChecked());
                dungeonGamesTab.setVisible(dungeonGamesTab.getTabButton().isChecked());

                tabBar.clearChildren();

                for (ImageButton button : tabButtons) {
                    if (button == selected) {
                        tabBar.add(button).width(tabSelectedWidth).growY();
                        button.getImageCell().size(56, 56);
                        button.invalidateHierarchy();
                    } else {
                        tabBar.add(button).width(tabWidth).growY();
                        button.getImageCell().size(42, 42);
                        button.invalidateHierarchy();
                    }
                }
            }
        };
        shopTab.getTabButton().addListener(tabListener);
        vaultThroneTab.getTabButton().addListener(tabListener);
        playTab.getTabButton().addListener(tabListener);
        factionTab.getTabButton().addListener(tabListener);
        dungeonGamesTab.getTabButton().addListener(tabListener);

        tabButtons.add(shopTab.getTabButton());
        tabButtons.add(vaultThroneTab.getTabButton());
        tabButtons.add(playTab.getTabButton());
        tabButtons.add(factionTab.getTabButton());
        tabButtons.add(dungeonGamesTab.getTabButton());

        ButtonGroup<Button> tabs = new ButtonGroup<>();
        tabs.setMaxCheckCount(1);

        for (int i = 0; i < tabButtons.size; ++i) { // We have to use this type of loop to avoid the error: #iterator() cannot be used nested
            tabs.add(tabButtons.get(i));
        }

        getStage().addActor(root);

        super.show();
    }

    @Override
    public void dispose() {
        super.dispose();
        skin.dispose();
    }

    public static class ShopTab extends Tab {
        protected ShopTab(Skin skin, Drawable tabBackground, Drawable tabSelectedBackground, TextureRegion tabTexture) {
            super(skin, tabBackground, tabSelectedBackground, tabTexture);
        }

        @Override
        protected void build() {
            add(new Label("Shop", getSkin())).pad(20).row();
        }
    }

    public static class VaultThroneTab extends Tab {
        protected VaultThroneTab(Skin skin, Drawable tabBackground, Drawable tabSelectedBackground, TextureRegion tabTexture) {
            super(skin, tabBackground, tabSelectedBackground, tabTexture);
        }

        @Override
        protected void build() {
            add(new Label("Vault & Throne", getSkin())).pad(20).row();
        }
    }

    public static class PlayTab extends Tab {
        protected PlayTab(Skin skin, Drawable tabBackground, Drawable tabSelectedBackground, TextureRegion tabTexture) {
            super(skin, tabBackground, tabSelectedBackground, tabTexture);
        }

        @Override
        protected void build() {
            add(new Label("Play", getSkin())).pad(20).row();
        }
    }

    public static class FactionTab extends Tab {
        protected FactionTab(Skin skin, Drawable tabBackground, Drawable tabSelectedBackground, TextureRegion tabTexture) {
            super(skin, tabBackground, tabSelectedBackground, tabTexture);
        }

        @Override
        protected void build() {
            add(new Label("Faction", getSkin())).pad(20).row();
        }
    }

    public static class DungeonGamesTab extends Tab {
        public DungeonGamesTab(Skin skin, Drawable tabBackground, Drawable tabSelectedBackground, TextureRegion tabTexture) {
            super(skin, tabBackground, tabSelectedBackground, tabTexture);
        }

        @Override
        protected void build() {
            add(new Label("Dungeon Games", getSkin())).pad(20).row();
        }
    }

    public abstract static class Tab extends Table {
        private final ImageButton tabButton;

        protected Tab(Skin skin, Drawable tabBackground, Drawable tabSelectedBackground, TextureRegion tabTexture) {
            super(skin);
            setFillParent(true);
            build();

            ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
            style.up = tabBackground;
            style.checked = tabSelectedBackground;
            style.imageUp = new TextureRegionDrawable(tabTexture);
            tabButton = new ImageButton(style);
            tabButton.getImageCell().size(48, 48);
        }

        public ImageButton getTabButton() {
            return tabButton;
        }

        protected abstract void build();
    }
}
