package dev.creoii.dungeoneer.client.render.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;

import java.io.IOException;

public class LoadingScreen extends AbstractScreen {
    private boolean initialized;
    private Label label;


    private boolean connecting;

    public LoadingScreen(Dungeoneer client) {
        super(client);
    }

    @Override
    public void show() {
        Table root = new Table();
        root.setFillParent(true);

        Label title = new Label("Dungeoneer", SKIN);
        label = new Label("Loading...", SKIN);

        root.add(title).padBottom(30).row();
        root.add(label);

        getStage().addActor(root);

        super.show();
    }

    @Override
    public void render(float delta) {
        if (!initialized && getClient().getAssets().getManager().update()) {
            initialized = true;

            getClient().getAssets().bindAtlases();
            getClient().getAssets().createTiledWalls();

            getClient().get().addListener(getClient().getNetworkHandler());
            getClient().get().start();

            startConnectionThread();
        }

        super.render(delta);
    }

    private void startConnectionThread() {
        if (connecting)
            return;

        connecting = true;

        Thread thread = new Thread(() -> {
            int attempt = 0;

            while (!isDisposed() && !getClient().get().isConnected()) {
                attempt++;
                int currentAttempt = attempt;

                Gdx.app.postRunnable(() -> label.setText("Connecting... attempt " + currentAttempt));

                try {
                    getClient().get().connect(5000, "localhost", 54555, 54777);
                } catch (IOException e) {
                    Dungeoneer.LOGGER.error("Connection attempt %s failed.", attempt, e);

                    Gdx.app.postRunnable(() -> label.setText("Connecting... attempt " + (currentAttempt + 1)));

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            Gdx.app.postRunnable(() -> {
                Dungeoneer.LOGGER.info("Client initialized.");
                getClient().getState().setStatus(ClientState.Status.AUTHENTICATING);
            });
            }, "Server Connection Thread");

        thread.setDaemon(true);
        thread.start();
    }
}
