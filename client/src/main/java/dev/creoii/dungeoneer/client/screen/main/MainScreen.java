package dev.creoii.dungeoneer.client.screen.main;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Array;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.screen.AbstractScreen;

import java.util.HashMap;
import java.util.Map;

public class MainScreen extends AbstractScreen {
    private final Map<ImageButton, Tab> buttonToTab;
    private Tab selectedTab;
    private Table tabBar;
    private float tabWidth;
    private float tabSelectedWidth;
    private final Array<ImageButton> tabButtons;

    public MainScreen(Dungeoneer client) {
        super(client);
        tabButtons = new Array<>(5);
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
        final ShopTab shopTab = new ShopTab(getClient(), getClient().getAssets().getTexture(Assets.Atlas.UI, "market"));
        final VaultThroneTab vaultThroneTab = new VaultThroneTab(getClient(), getClient().getAssets().getTexture(Assets.Atlas.UI, "throne"));
        final PlayTab playTab = new PlayTab(getClient(), getClient().getAssets().getTexture(Assets.Atlas.UI, "chest"));
        final FactionTab factionTab = new FactionTab(getClient(), getClient().getAssets().getTexture(Assets.Atlas.UI, "tower"));
        final DungeonGamesTab dungeonGamesTab = new DungeonGamesTab(getClient(), getClient().getAssets().getTexture(Assets.Atlas.UI, "skull"));
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

        getClient().getInputMultiplexer().addProcessor(getStage());
    }

    @Override
    public void hide() {
        getClient().getInputMultiplexer().removeProcessor(getStage());
    }

    @Override
    public void dispose() {
        super.dispose();
        SKIN.dispose();
    }
}
