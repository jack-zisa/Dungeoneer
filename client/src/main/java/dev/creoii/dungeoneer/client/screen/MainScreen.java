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

    public MainScreen() {
        tab.scale(4f, 4f);
        tabSelected.scale(4f, 4f);
    }

    @Override
    public void show() {
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        float width = getStage().getWidth();
        float selectedWidth = width / 3f;
        float otherWidth = width / 6f;

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

        Table tabBar = new Table();
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

                for (Button tab : new Button[]{shopTab.getTabButton(), vaultThroneTab.getTabButton(), playTab.getTabButton(), factionTab.getTabButton(), dungeonGamesTab.getTabButton()}) {
                    tabBar.add(tab).width(tab == selected ? selectedWidth : otherWidth).growY();
                }
            }
        };
        shopTab.getTabButton().addListener(tabListener);
        vaultThroneTab.getTabButton().addListener(tabListener);
        playTab.getTabButton().addListener(tabListener);
        factionTab.getTabButton().addListener(tabListener);
        dungeonGamesTab.getTabButton().addListener(tabListener);

        ButtonGroup<Button> tabs = new ButtonGroup<>();
        tabs.setMinCheckCount(1);
        tabs.setMaxCheckCount(1);

        tabs.add(shopTab.getTabButton());
        tabs.add(vaultThroneTab.getTabButton());
        tabs.add(playTab.getTabButton());
        tabs.add(factionTab.getTabButton());
        tabs.add(dungeonGamesTab.getTabButton());

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
