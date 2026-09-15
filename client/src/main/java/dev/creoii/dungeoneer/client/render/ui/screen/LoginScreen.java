package dev.creoii.dungeoneer.client.render.ui.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.network.c2s.account.LoginC2S;

public class LoginScreen extends AbstractScreen {
    private TextField usernameField;
    private TextField passwordField;
    private Label statusLabel;

    public LoginScreen(Dungeoneer client) {
        super(client);
    }

    @Override
    public void show() {
        Table root = new Table();
        root.setFillParent(true);

        Label title = new Label("Dungeoneer", SKIN);

        usernameField = new TextField("", SKIN);
        usernameField.setMessageText("Username");

        passwordField = new TextField("", SKIN);
        passwordField.setMessageText("Password");
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');

        TextButton loginButton = new TextButton("Login", SKIN);

        statusLabel = new Label("", SKIN);

        loginButton.addListener(
            new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    String username = usernameField.getText();
                    String password = passwordField.getText();

                    statusLabel.setText("Logging in...");
                    getClient().get().sendTCP(new LoginC2S(username, password));
                }
            }
        );

        root.add(title).padBottom(30).row();
        root.add(usernameField).width(300).padBottom(10).row();
        root.add(passwordField).width(300).padBottom(20).row();
        root.add(loginButton).width(300).height(40).padBottom(10).row();
        root.add(statusLabel);

        getStage().addActor(root);

        getClient().getInputMultiplexer().addProcessor(getStage());
    }

    @Override
    public void hide() {
        getClient().getInputMultiplexer().removeProcessor(getStage());
    }
}
