package dev.creoii.dungeoneer.client.render.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import dev.creoii.dungeoneer.client.Dungeoneer;

public abstract class AbstractScreen implements Screen {
    public static final Skin SKIN = new Skin(Gdx.files.internal("uiskin.json"));
    private final Dungeoneer client;
    private final Stage stage;
    private boolean disposed;

    public AbstractScreen(Dungeoneer client) {
        this.client = client;
        stage = new Stage(new ScreenViewport());
        disposed = false;
    }

    public Dungeoneer getClient() {
        return client;
    }

    public Stage getStage() {
        return stage;
    }

    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        getStage().act(delta);
        getStage().draw();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        getStage().getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        getStage().dispose();
        SKIN.dispose();
        disposed = true;
    }
}
