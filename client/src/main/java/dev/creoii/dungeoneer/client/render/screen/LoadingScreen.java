package dev.creoii.dungeoneer.client.render.screen;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.network.PacketSerializer;

import java.io.IOException;

public class LoadingScreen extends AbstractScreen {
    private boolean initialized;

    public LoadingScreen(Dungeoneer client) {
        super(client);
    }

    @Override
    public void show() {
        Table root = new Table();
        root.setFillParent(true);

        Label title = new Label("Dungeoneer", SKIN);
        Label loadingLabel = new Label("Loading...", SKIN);

        root.add(title).padBottom(30).row();
        root.add(loadingLabel);

        getStage().addActor(root);

        super.show();
    }

    @Override
    public void render(float delta) {
        if (!initialized && getClient().getAssets().getManager().update()) {
            initialized = true;

            getClient().getAssets().bindAtlases();
            getClient().getAssets().createTiledWalls();

            PacketSerializer.registerDefault(getClient().get().getKryo());

            getClient().get().addListener(getClient().getNetworkHandler());
            getClient().get().start();

            try {
                getClient().get().connect(5000, "localhost", 54555, 54777);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Dungeoneer.LOGGER.info("Client initialized.");
            getClient().getState().setStatus(ClientState.Status.AUTHENTICATING);
        }

        super.render(delta);
    }
}
