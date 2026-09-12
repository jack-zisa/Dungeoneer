package dev.creoii.dungeoneer.client.render.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import dev.creoii.dungeoneer.client.Dungeoneer;

public abstract class AbstractScreen implements Screen {
    public static final Skin SKIN = new Skin(Gdx.files.internal("uiskin.json"));
    private final Dungeoneer client;
    private final Stage stage;
    private boolean disposed;
    private final Image fadeOverlay;

    public AbstractScreen(Dungeoneer client) {
        this.client = client;
        stage = new Stage(new ScreenViewport());
        disposed = false;

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();

        Texture whiteTexture = new Texture(pixmap);
        pixmap.dispose();

        fadeOverlay = new Image(whiteTexture);
        fadeOverlay.setColor(Color.BLACK);
        fadeOverlay.setSize(stage.getWidth(), stage.getHeight());

        fadeOverlay.getColor().a = 0f;

        stage.addActor(fadeOverlay);
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

    public void fadeToBlack(float alpha, float duration) {
        fadeOverlay.toFront();
        fadeOverlay.addAction(Actions.alpha(alpha, duration));
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
