package dev.creoii.dungeoneer.client;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.utils.ScreenUtils;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Listener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.creoii.dungeoneer.client.control.CharacterInputListener;
import dev.creoii.dungeoneer.client.option.Settings;
import dev.creoii.dungeoneer.client.screen.LoadingScreen;
import dev.creoii.dungeoneer.network.CreoSerialization;
import dev.creoii.dungeoneer.util.logging.Logger;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Dungeoneer extends Game {
    public static final Logger LOGGER = new Logger(Dungeoneer.class.getSimpleName());
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Client client;
    private final ClientState state;
    private Assets assets;
    private final CharacterInputListener controller;
    private final Settings settings;
    private final Listener.QueuedListener listener;

    public Dungeoneer() {
        client = new Client(256 * 1024, 256 * 1024, new CreoSerialization());
        state = new ClientState(this);
        controller = new CharacterInputListener(this);
        settings = Settings.DEFAULT;
        listener = new Listener.QueuedListener(new ClientListener(this)) {
            @Override
            protected void queue(Runnable runnable) {
                runnable.run();
            }
        };
    }

    public Client get() {
        return client;
    }

    public ClientState getState() {
        return state;
    }

    public Assets getAssets() {
        return assets;
    }

    public CharacterInputListener getController() {
        return controller;
    }

    public Settings getSettings() {
        return settings;
    }

    public Listener.QueuedListener getListener() {
        return listener;
    }

    @Override
    public void create() {
        TooltipManager.getInstance().animations = false;
        TooltipManager.getInstance().resetTime = 0f;
        TooltipManager.getInstance().initialTime = 0f;
        TooltipManager.getInstance().offsetX = 0f;
        TooltipManager.getInstance().offsetY = 0f;

        final LoadingScreen loadingScreen = new LoadingScreen(this);
        setScreen(loadingScreen);

        state.setStatus(ClientState.Status.LOADING);

        settings.load();
        assets = new Assets();
        assets.load();
    }

    @Override
    public void render() {
        if (assets.getManager().update(17)) {
            ScreenUtils.clear(0, 0, 0, 1);
            super.render();
        }
    }
}
