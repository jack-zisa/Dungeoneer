package dev.creoii.dungeoneer.client.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.*;

public class LoadingScreen extends AbstractScreen {
    private Skin skin;

    @Override
    public void show() {
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        Table root = new Table();
        root.setFillParent(true);

        Label title = new Label("Dungeoneer", skin);
        Label loadingLabel = new Label("Loading...", skin);

        root.add(title).padBottom(30).row();
        root.add(loadingLabel);

        getStage().addActor(root);

        super.show();
    }

    @Override
    public void dispose() {
        super.dispose();
        skin.dispose();
    }
}
