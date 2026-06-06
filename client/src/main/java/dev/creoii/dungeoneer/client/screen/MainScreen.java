package dev.creoii.dungeoneer.client.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import dev.creoii.dungeoneer.client.AssetManager;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.definitions.Character;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainScreen extends AbstractScreen {
    private static final NinePatchDrawable TAB_BACKGROUND = new NinePatchDrawable(AssetManager.TAB_9PATCH);
    private static final NinePatchDrawable TAB_SELECTED_BACKGROUND = new NinePatchDrawable(AssetManager.TAB_SELECTED_9PATCH);
    private static final Skin SKIN = new Skin(Gdx.files.internal("uiskin.json"));

    private final Dungeoneer client;
    private final Map<ImageButton, Tab> buttonToTab;
    private Tab selectedTab;
    private Table tabBar;
    private float tabWidth;
    private float tabSelectedWidth;
    private final Array<ImageButton> tabButtons;

    public MainScreen(Dungeoneer client) {
        this.client = client;
        tabButtons = new Array<>(5);

        AssetManager.TAB_9PATCH.scale(4f, 4f);
        AssetManager.TAB_SELECTED_9PATCH.scale(4f, 4f);

        buttonToTab = new HashMap<>(5);
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
        float stageWidth = getStage().getWidth();
        tabWidth = stageWidth / 6f;
        tabSelectedWidth = stageWidth / 3f;

        Table root = new Table();
        root.setFillParent(true);

        Stack content = new Stack();
        final ShopTab shopTab = new ShopTab(client, AssetManager.MARKET_TEXTURE);
        final VaultThroneTab vaultThroneTab = new VaultThroneTab(client, AssetManager.THRONE_TEXTURE);
        final PlayTab playTab = new PlayTab(client, AssetManager.CHEST_TEXTURE);
        final FactionTab factionTab = new FactionTab(client, AssetManager.TOWER_TEXTURE);
        final DungeonGamesTab dungeonGamesTab = new DungeonGamesTab(client, AssetManager.SKULL_TEXTURE);
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
        SKIN.dispose();
    }

    public static class ShopTab extends Tab {
        protected ShopTab(Dungeoneer client, TextureRegion tabTexture) {
            super(client, tabTexture);
        }

        @Override
        protected void build() {
            add(new Label("Shop", getSkin()));
        }
    }

    public static class VaultThroneTab extends Tab {
        private int classIndex = 0;
        private java.util.List<Character> characters;
        private Label classLabel;
        private Stack[] classIcons;
        private Table carousel;

        protected VaultThroneTab(Dungeoneer client, TextureRegion tabTexture) {
            super(client, tabTexture);
        }

        @Override
        public void init() {
            characters = new ArrayList<>();
        }

        @Override
        protected void build() {
            add(new Label("Vault & Throne", getSkin())).pad(20).row();

            classIcons = new Stack[]{null, null, null};
            carousel = new Table();

            characters.addAll(getClient().getState().getCharacters());

            classLabel = new Label("", getSkin());
            for (int i = 0; i < 3; i++) {
                classIcons[i] = new Stack();
                classIcons[i].add(new Image(new NinePatchDrawable(AssetManager.TAB_9PATCH)));
                classIcons[i].add(new Image(new TextureRegionDrawable(AssetManager.MISSING_TEXTURE)));
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
            center.add(classIcons[1]).size(120).row();
            center.add(classLabel).padTop(10);

            carousel.add(previous).width(40);
            carousel.add(classIcons[0]).size(96).expandX().pad(10);
            carousel.add(center).expandX().pad(20);
            carousel.add(classIcons[2]).size(96).expandX().pad(10);
            carousel.add(next).width(40);

            add(carousel).growX().height(200).padBottom(20).row();

            TextButton button = new TextButton("Create Character", getSkin());
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    new CreateCharacterDialog(getClient(), getSkin()).show(getStage());
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
                    classIcons[i].removeActorAt(1, true);
                    Container<Image> container = new Container<>(new Image(new TextureRegionDrawable(AssetManager.MISSING_TEXTURE)));
                    container.size(48f);
                    classIcons[i].add(container);
                }
                return;
            }

            int size = characters.size();

            int left = size > 2 ? (classIndex - 1 + size) % size : -1;
            int center = classIndex;
            int right = size > 1 ? (classIndex + 1) % size : -1;

            int[] indices = {left, center, right};

            classLabel.setText(characters.get(center).characterClass().id());
            for (int i = 0; i < 3; i++) {
                if (indices[i] == -1)
                    continue;
                classIcons[i].removeActorAt(1, true);
                Container<Image> container = new Container<>(new Image(new TextureRegionDrawable(getClassTexture(characters.get(indices[i]).characterClass().id()))));
                container.size(48f);
                classIcons[i].add(container);

                classIcons[i].add(container);
            }

            classIcons[0].setVisible(characters.size() > 2);
            classIcons[2].setVisible(characters.size() > 1);
        }

        private TextureRegion getClassTexture(String classId) {
            return switch (classId) {
                case "wizard" -> AssetManager.WIZARD_TEXTURE;
                case "knight" -> AssetManager.KNIGHT_TEXTURE;
                case "ninja" -> AssetManager.NINJA_TEXTURE;
                case "priest" -> AssetManager.PRIEST_TEXTURE;
                case "rogue" -> AssetManager.ROGUE_TEXTURE;
                case "archer" -> AssetManager.ARCHER_TEXTURE;
                default -> AssetManager.MISSING_TEXTURE;
            };
        }
    }

    public static class PlayTab extends Tab {
        protected PlayTab(Dungeoneer client, TextureRegion tabTexture) {
            super(client, tabTexture);
        }

        @Override
        protected void build() {
            Table statsTable = new Table();

            Label powerLabel = new Label(String.format("Power: %s", 0), getSkin());
            Label goldLabel = new Label(String.format("Gold: %s", 0), getSkin());
            Label gemsLabel = new Label(String.format("Gems: %s", 0), getSkin());

            statsTable.add(powerLabel).left().pad(10f);
            statsTable.add(goldLabel).left().pad(10f);
            statsTable.add(gemsLabel).left().pad(10f);

            Table accountTable = new Table();
            accountTable.add(new Label(getClient().getState().getAccount().username(), getSkin())).left().pad(10f);

            TextButton settingsButton = new TextButton("Settings", getSkin());
            settingsButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    new SettingsDialog(getClient(), getSkin()).show(getStage());
                }
            });
            accountTable.add(settingsButton).pad(10f);

            add(statsTable).width(96f).height(32f).top().expandX().fillX().row();
            add(accountTable).width(96f).height(32f).top().expandX().fillX().row();

            Table mainSection = new Table();
            mainSection.add(new TextButton("Raid", getSkin()));
            add(mainSection).expand().fill();
        }
    }

    public static class FactionTab extends Tab {
        protected FactionTab(Dungeoneer client, TextureRegion tabTexture) {
            super(client, tabTexture);
        }

        @Override
        protected void build() {
            add(new Label("Faction", getSkin()));
        }
    }

    public static class DungeonGamesTab extends Tab {
        public DungeonGamesTab(Dungeoneer client, TextureRegion tabTexture) {
            super(client, tabTexture);
        }

        @Override
        protected void build() {
            add(new Label("Dungeon Games", getSkin()));
        }
    }

    public abstract static class Tab extends Table {
        private final Dungeoneer client;
        private final ImageButton tabButton;

        protected Tab(Dungeoneer client, TextureRegion tabTexture) {
            super(SKIN);
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
