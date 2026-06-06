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
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.definitions.Character;
import dev.creoii.dungeoneer.definitions.CharacterClass;
import dev.creoii.dungeoneer.network.c2s.account.CreateCharacterC2S;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainScreen extends AbstractScreen {
    private static final TextureRegion marketTexture = new TextureRegion(new Texture("textures/ui/market.png"));
    private static final TextureRegion throneTexture = new TextureRegion(new Texture("textures/ui/throne.png"));
    private static final TextureRegion chestTexture = new TextureRegion(new Texture("textures/ui/chest.png"));
    private static final TextureRegion towerTexture = new TextureRegion(new Texture("textures/ui/tower.png"));
    private static final TextureRegion skullTexture = new TextureRegion(new Texture("textures/ui/skull.png"));

    private static final TextureRegion wizardTexture = new TextureRegion(new Texture("textures/character/wizard.png"));
    private static final TextureRegion knightTexture = new TextureRegion(new Texture("textures/character/knight.png"));
    private static final TextureRegion archerTexture = new TextureRegion(new Texture("textures/character/archer.png"));
    private static final TextureRegion rogueTexture = new TextureRegion(new Texture("textures/character/rogue.png"));
    private static final TextureRegion priestTexture = new TextureRegion(new Texture("textures/character/priest.png"));
    private static final TextureRegion ninjaTexture = new TextureRegion(new Texture("textures/character/ninja.png"));

    private static final TextureRegion missingTexture = new TextureRegion(new Texture("textures/misc/missing.png"));

    private final NinePatch tab = new NinePatch(new Texture("textures/ui/tab.png"), 2, 2, 2 ,2);
    private final NinePatch tabSelected = new NinePatch(new Texture("textures/ui/tab_selected.png"), 2, 2, 2 ,2);

    private final Dungeoneer client;

    private Skin skin;

    private final Map<ImageButton, Tab> buttonToTab;
    private Tab selectedTab;

    private Table tabBar;
    private float tabWidth;
    private float tabSelectedWidth;
    private final Array<ImageButton> tabButtons;

    public MainScreen(Dungeoneer client) {
        this.client = client;
        tabButtons = new Array<>(5);

        tab.scale(4f, 4f);
        tabSelected.scale(4f, 4f);

        buttonToTab = new HashMap<>();
    }

    public Tab getSelectedTab() {
        return selectedTab;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        float stageWidth = getStage().getWidth();
        tabWidth = stageWidth / 6f;
        tabSelectedWidth = stageWidth / 3f;

        tabBar.clearChildren();

        for (ImageButton button : tabButtons) {
            tabBar.add(button).width(buttonToTab.get(button) == selectedTab ? tabSelectedWidth : tabWidth).growY();
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
        final ShopTab shopTab = new ShopTab(client, skin, tabBackground, tabSelectedBackground, marketTexture);
        final VaultThroneTab vaultThroneTab = new VaultThroneTab(client, skin, tabBackground, tabSelectedBackground, throneTexture);
        final PlayTab playTab = new PlayTab(client, skin, tabBackground, tabSelectedBackground, chestTexture);
        final FactionTab factionTab = new FactionTab(client, skin, tabBackground, tabSelectedBackground, towerTexture);
        final DungeonGamesTab dungeonGamesTab = new DungeonGamesTab(client, skin, tabBackground, tabSelectedBackground, skullTexture);
        content.addActor(shopTab);
        content.addActor(vaultThroneTab);
        content.addActor(playTab);
        content.addActor(factionTab);
        content.addActor(dungeonGamesTab);
        root.add(content).expand().fill().row();

        buttonToTab.put(shopTab.getTabButton(), shopTab);
        buttonToTab.put(vaultThroneTab.getTabButton(), vaultThroneTab);
        buttonToTab.put(playTab.getTabButton(), playTab);
        buttonToTab.put(factionTab.getTabButton(), factionTab);
        buttonToTab.put(dungeonGamesTab.getTabButton(), dungeonGamesTab);

        tabBar = new Table();
        root.add(tabBar).growX().height(60);
        tabBar.defaults().expandX().fillX().height(60);
        tabBar.add(shopTab.getTabButton()).expandX().fillX();
        tabBar.add(vaultThroneTab.getTabButton()).expandX().fillX();
        tabBar.add(playTab.getTabButton()).expandX().fillX();
        tabBar.add(factionTab.getTabButton()).expandX().fillX();
        tabBar.add(dungeonGamesTab.getTabButton()).expandX().fillX();

        ChangeListener tabListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!(actor instanceof ImageButton buttonActor))
                    return;

                selectedTab = buttonToTab.get(buttonActor);
                selectedTab.select();

                shopTab.setVisible(selectedTab == shopTab);
                vaultThroneTab.setVisible(selectedTab == vaultThroneTab);
                playTab.setVisible(selectedTab == playTab);
                factionTab.setVisible(selectedTab == factionTab);
                dungeonGamesTab.setVisible(selectedTab == dungeonGamesTab);

                tabBar.clearChildren();

                for (ImageButton button : tabButtons) {
                    if (buttonToTab.get(button) == selectedTab) {
                        tabBar.add(button).width(tabSelectedWidth).growY();
                        button.getImageCell().size(56, 56);
                    } else {
                        tabBar.add(button).width(tabWidth).growY();
                        button.getImageCell().size(42, 42);
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

        playTab.getTabButton().setChecked(true);

        getStage().addActor(root);

        super.show();
    }

    @Override
    public void dispose() {
        super.dispose();
        skin.dispose();
    }

    public static class ShopTab extends Tab {
        protected ShopTab(Dungeoneer client, Skin skin, Drawable tabBackground, Drawable tabSelectedBackground, TextureRegion tabTexture) {
            super(client, skin, tabBackground, tabSelectedBackground, tabTexture);
        }

        @Override
        protected void build() {
            add(new Label("Shop", getSkin())).pad(20).row();
        }
    }

    public static class VaultThroneTab extends Tab {
        private int classIndex = 0;
        private java.util.List<Character> characters;
        private Label classLabel;
        private Image[] classIcons;
        private Table carousel;

        protected VaultThroneTab(Dungeoneer client, Skin skin, Drawable tabBackground, Drawable tabSelectedBackground, TextureRegion tabTexture) {
            super(client, skin, tabBackground, tabSelectedBackground, tabTexture);
        }

        @Override
        public void init() {
            characters = new ArrayList<>();
        }

        @Override
        protected void build() {
            add(new Label("Vault & Throne", getSkin())).pad(20).row();

            classIcons = new Image[]{null, null, null};
            carousel = new Table();

            characters.addAll(getClient().getState().getCharacters());

            classLabel = new Label("", getSkin());
            for (int i = 0; i < 3; i++) {
                classIcons[i] = new Image(new TextureRegionDrawable(missingTexture));
            }

            TextButton previous = new TextButton("<", getSkin());
            TextButton next = new TextButton(">", getSkin());

            previous.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (characters.isEmpty())
                        return;

                    classIndex--;

                    if (classIndex < 0) {
                        classIndex = characters.size() - 1;
                    }

                    classLabel.setText(classIndex + ": " + characters.get(classIndex).characterClass().id());

                    updateCharacterDisplay();
                }
            });

            next.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (characters.isEmpty())
                        return;
                    classIndex++;

                    if (classIndex >= characters.size()) {
                        classIndex = 0;
                    }

                    classLabel.setText(classIndex + ": " + characters.get(classIndex).characterClass().id());
                    updateCharacterDisplay();
                }
            });

            Table center = new Table();
            center.add(classIcons[1]).size(72).row();
            center.add(classLabel).padTop(10);

            carousel.add(previous).width(40);
            carousel.add(classIcons[0]).size(48).expandX().pad(10);
            carousel.add(center).expandX().pad(20);
            carousel.add(classIcons[2]).size(48).expandX().pad(10);
            carousel.add(next).width(40);

            add(carousel).growX().height(200).padBottom(20).row();

            TextButton button = new TextButton("Create Character", getSkin());
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    getClient().get().sendUDP(new CreateCharacterC2S(getClient().getState().getAccount().id(), CharacterClass.random()));
                }
            });
            add(button);
        }

        @Override
        public void select() {
            refreshCharacters();
            updateCharacterDisplay();
        }

        public void refreshCharacters() {
            characters.clear();
            characters.addAll(getClient().getState().getCharacters());

            carousel.setVisible(!characters.isEmpty());

            if (!characters.isEmpty()) {
                classLabel.setText(classIndex + ": " + characters.get(classIndex).characterClass().id());
            }
        }

        private void updateCharacterDisplay() {
            if (characters.isEmpty()) {
                classLabel.setText("");
                for (int i = 0; i < 3; i++) {
                    classIcons[i].setDrawable(new TextureRegionDrawable(missingTexture));
                }
                return;
            }

            int size = characters.size();

            int left = (classIndex - 1 + size) % size;
            int center = classIndex;
            int right = (classIndex + 1) % size;

            int[] indices = {left, center, right};

            classLabel.setText(characters.get(indices[1]).characterClass().id());
            for (int i = 0; i < 3; i++) {
                classIcons[i].setDrawable(new TextureRegionDrawable(getClassTexture(characters.get(indices[i]).characterClass().id())));
            }
        }

        private TextureRegion getClassTexture(String classId) {
            return switch (classId) {
                case "wizard" -> wizardTexture;
                case "knight" -> knightTexture;
                case "ninja" -> ninjaTexture;
                case "priest" -> priestTexture;
                case "rogue" -> rogueTexture;
                case "archer" -> archerTexture;
                default -> missingTexture;
            };
        }
    }

    public static class PlayTab extends Tab {
        protected PlayTab(Dungeoneer client, Skin skin, Drawable tabBackground, Drawable tabSelectedBackground, TextureRegion tabTexture) {
            super(client, skin, tabBackground, tabSelectedBackground, tabTexture);
        }

        @Override
        protected void build() {
            add(new Label("Play", getSkin())).pad(20).row();
        }
    }

    public static class FactionTab extends Tab {
        protected FactionTab(Dungeoneer client, Skin skin, Drawable tabBackground, Drawable tabSelectedBackground, TextureRegion tabTexture) {
            super(client, skin, tabBackground, tabSelectedBackground, tabTexture);
        }

        @Override
        protected void build() {
            add(new Label("Faction", getSkin())).pad(20).row();
        }
    }

    public static class DungeonGamesTab extends Tab {
        public DungeonGamesTab(Dungeoneer client, Skin skin, Drawable tabBackground, Drawable tabSelectedBackground, TextureRegion tabTexture) {
            super(client, skin, tabBackground, tabSelectedBackground, tabTexture);
        }

        @Override
        protected void build() {
            add(new Label("Dungeon Games", getSkin())).pad(20).row();
        }
    }

    public abstract static class Tab extends Table {
        private final Dungeoneer client;
        private final ImageButton tabButton;

        protected Tab(Dungeoneer client, Skin skin, Drawable tabBackground, Drawable tabSelectedBackground, TextureRegion tabTexture) {
            super(skin);
            this.client = client;

            setFillParent(true);
            init();
            build();

            ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
            style.up = tabBackground;
            style.checked = tabSelectedBackground;
            style.imageUp = new TextureRegionDrawable(tabTexture);
            tabButton = new ImageButton(style);
            tabButton.getImageCell().size(48, 48);
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
}
