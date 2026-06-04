package dev.creoii.dungeoneer.client.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.network.c2s.account.LoginC2S;

public class LoginScreen implements Screen {
    private final Dungeoneer client;
    private Stage stage;
    private Skin skin;

    private TextField usernameField;
    private TextField passwordField;
    private Label statusLabel;

    public LoginScreen(Dungeoneer client) {
        this.client = client;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        Table root = new Table();
        root.setFillParent(true);

        Label title = new Label("Dungeoneer", skin);

        usernameField = new TextField("", skin);
        usernameField.setMessageText("Username");

        passwordField = new TextField("", skin);
        passwordField.setMessageText("Password");
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');

        TextButton loginButton = new TextButton("Login", skin);

        statusLabel = new Label("", skin);

        loginButton.addListener(
            new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    String username = usernameField.getText();
                    String password = passwordField.getText();

                    statusLabel.setText("Logging in...");
                    client.get().sendUDP(new LoginC2S(username, password));
                }
            }
        );

        root.add(title).padBottom(30).row();
        root.add(usernameField).width(300).padBottom(10).row();
        root.add(passwordField).width(300).padBottom(20).row();
        root.add(loginButton).width(300).height(40).padBottom(10).row();
        root.add(statusLabel);

        stage.addActor(root);

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        stage.getViewport().update(width, height, true);
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
        stage.dispose();
        skin.dispose();
    }
}
