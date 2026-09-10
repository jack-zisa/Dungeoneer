package dev.creoii.dungeoneer.client;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.utils.ScreenUtils;
import com.esotericsoftware.kryonet.Client;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.creoii.dungeoneer.client.command.ClientCommandManager;
import dev.creoii.dungeoneer.client.network.ClientNetworkHandler;
import dev.creoii.dungeoneer.client.option.Settings;
import dev.creoii.dungeoneer.client.render.DebugRenderer;
import dev.creoii.dungeoneer.client.render.screen.AbstractScreen;
import dev.creoii.dungeoneer.client.render.screen.LoadingScreen;
import dev.creoii.dungeoneer.client.render.screen.game.GameScreen;
import dev.creoii.dungeoneer.network.CreoSerialization;
import dev.creoii.dungeoneer.util.logging.Logger;

import java.util.Random;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Dungeoneer extends Game {
    public static final Logger LOGGER = new Logger(Dungeoneer.class.getSimpleName());
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Random RANDOM = new Random();
    private final Client client;
    private final ClientState state;
    private final InputMultiplexer inputMultiplexer;
    private Assets assets;
    private final Settings settings;
    private final ClientNetworkHandler networkHandler;
    private final ClientCommandManager commandManager;
    private final DebugRenderer debugRenderer;
    private boolean debug;

    public Dungeoneer() {
        client = new Client(256 * 1024, 256 * 1024, new CreoSerialization());
        state = new ClientState(this);
        inputMultiplexer = new InputMultiplexer();
        settings = Settings.DEFAULT;
        networkHandler = new ClientNetworkHandler(this);
        commandManager = new ClientCommandManager(this);
        debugRenderer = new DebugRenderer(this);
    }

    public Client get() {
        return client;
    }

    public ClientState getState() {
        return state;
    }

    public InputMultiplexer getInputMultiplexer() {
        return inputMultiplexer;
    }

    public Assets getAssets() {
        return assets;
    }

    public Settings getSettings() {
        return settings;
    }

    public ClientNetworkHandler getNetworkHandler() {
        return networkHandler;
    }

    @Override
    public void create() {
        Gdx.input.setInputProcessor(inputMultiplexer);

        inputMultiplexer.addProcessor(commandManager);

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
        ScreenUtils.clear(0, 0, 0, 1, true);

        float dt = Gdx.graphics.getDeltaTime();

        networkHandler.tick(dt);

        if (state.getStatus() == ClientState.Status.RAIDING && !state.getCurrentRaid().isNull() && !state.getActiveCharacter().isNull() && screen instanceof GameScreen gameScreen) {
            state.getCurrentRaid().update(dt);
        }

        super.render();

        if (screen instanceof AbstractScreen abstractScreen && Assets.FONT != null) {
            abstractScreen.getStage().getBatch().begin();

            if (commandManager.isActive()) commandManager.render(abstractScreen);

            if (Gdx.input.isKeyJustPressed(settings.debugKey().value())) {
                debug = !debug;
            }

            if (debug) debugRenderer.render(abstractScreen);

            abstractScreen.getStage().getBatch().end();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        client.stop();
    }
}
