package dev.creoii.dungeoneer.client.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class MainScreen extends AbstractScreen {
    private Skin skin;
    private Actor selected;

    @Override
    public void show() {
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        float width = getStage().getWidth();
        float selectedWidth = width / 3f;
        float otherWidth = width / 6f;

        Table root = new Table();
        root.setFillParent(true);

        Label title = new Label("Dungeoneer", skin);
        root.add(title).padBottom(30).row();

        Stack content = new Stack();
        final Image shopContent = new Image(skin.newDrawable("white", .5f,0,0,1));
        final Image vaultThroneContent = new Image(skin.newDrawable("white", 0,.5f,0,1));
        final Image playContent = new Image(skin.newDrawable("white", 0,0,.5f,1));
        final Image factionContent = new Image(skin.newDrawable("white", .5f,0,.5f,1));
        final Image dungeonGamesContent = new Image(skin.newDrawable("white", 0,.5f,.5f,1));
        content.addActor(shopContent);
        content.addActor(vaultThroneContent);
        content.addActor(playContent);
        content.addActor(factionContent);
        content.addActor(dungeonGamesContent);
        root.add(content).expand().fill().row();

        Table tabBar = new Table();
        final Button shopTabButton = new TextButton("Shop", skin, "toggle");
        final Button vaultThroneTabButton = new TextButton("Vault & Throne", skin, "toggle");
        final Button playTabButton = new TextButton("Play", skin, "toggle");
        final Button factionTabButton = new TextButton("Faction", skin, "toggle");
        final Button dungeonGamesTabButton = new TextButton("Dungeon Games", skin, "toggle");
        root.add(tabBar).growX().height(80);
        tabBar.defaults().expandX().fillX().height(80);
        tabBar.add(shopTabButton).expandX().fillX();
        tabBar.add(vaultThroneTabButton).expandX().fillX();
        tabBar.add(playTabButton).expandX().fillX();
        tabBar.add(factionTabButton).expandX().fillX();
        tabBar.add(dungeonGamesTabButton).expandX().fillX();
        selected = shopTabButton;

        ChangeListener tabListener = new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selected = actor;

                shopContent.setVisible(shopTabButton.isChecked());
                vaultThroneContent.setVisible(vaultThroneTabButton.isChecked());
                playContent.setVisible(playTabButton.isChecked());
                factionContent.setVisible(factionTabButton.isChecked());
                dungeonGamesContent.setVisible(dungeonGamesTabButton.isChecked());

                tabBar.clearChildren();

                for (Button tab : new Button[]{shopTabButton, vaultThroneTabButton, playTabButton, factionTabButton, dungeonGamesTabButton}) {
                    tabBar.add(tab).width(tab == selected ? selectedWidth : otherWidth).growY();
                }
            }
        };
        shopTabButton.addListener(tabListener);
        vaultThroneTabButton.addListener(tabListener);
        playTabButton.addListener(tabListener);
        factionTabButton.addListener(tabListener);
        dungeonGamesTabButton.addListener(tabListener);

        ButtonGroup<Button> tabs = new ButtonGroup<>();
        tabs.setMinCheckCount(1);
        tabs.setMaxCheckCount(1);
        tabs.add(shopTabButton);
        tabs.add(vaultThroneTabButton);
        tabs.add(playTabButton);
        tabs.add(factionTabButton);
        tabs.add(dungeonGamesTabButton);

        getStage().addActor(root);

        super.show();
    }

    @Override
    public void dispose() {
        super.dispose();
        skin.dispose();
    }

    public static class MainTab {

    }
}
