package dev.creoii.dungeoneer.client.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.network.c2s.account.LoginC2S;

public class LoginScreen extends AbstractScreen {
    private final Dungeoneer client;
    private Skin skin;

    private TextField usernameField;
    private TextField passwordField;
    private Label statusLabel;

    public LoginScreen(Dungeoneer client) {
        this.client = client;
    }

    @Override
    public void show() {
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

        getStage().addActor(root);

        super.show();
    }

    @Override
    public void dispose() {
        super.dispose();
        skin.dispose();
    }
}
