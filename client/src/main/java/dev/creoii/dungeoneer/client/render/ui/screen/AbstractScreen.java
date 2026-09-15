package dev.creoii.dungeoneer.client.render.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import dev.creoii.dungeoneer.client.Dungeoneer;
import org.jspecify.annotations.Nullable;

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

    public void fadeToBlack(float alpha, float duration, @Nullable Action finishAction) {
        fadeOverlay.toFront();
        fadeOverlay.addAction(Actions.alpha(alpha, duration));
        if (finishAction != null) fadeOverlay.addAction(Actions.delay(duration, finishAction));
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

    static {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/PressStart2P-vaV7.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 10;
        parameter.spaceX = -1;
        parameter.padTop = 4;
        parameter.padBottom = 4;

        BitmapFont font = generator.generateFont(parameter);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        SKIN.add("default-font", font, BitmapFont.class);

        SKIN.get(Label.LabelStyle.class).font = font;
        SKIN.get(TextButton.TextButtonStyle.class).font = font;
        SKIN.get(TextField.TextFieldStyle.class).font = font;
        SKIN.get(CheckBox.CheckBoxStyle.class).font = font;
        SKIN.get(SelectBox.SelectBoxStyle.class).font = font;
        SKIN.get(Window.WindowStyle.class).titleFont = font;
        SKIN.get(List.ListStyle.class).font = font;
    }
}
