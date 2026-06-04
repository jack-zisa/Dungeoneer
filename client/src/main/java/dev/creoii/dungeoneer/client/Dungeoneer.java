package dev.creoii.dungeoneer.client;

import com.badlogic.gdx.Game;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Listener;
import dev.creoii.dungeoneer.client.screen.LoadingScreen;
import dev.creoii.dungeoneer.network.CreoSerialization;
import dev.creoii.dungeoneer.network.PacketSerializer;
import dev.creoii.dungeoneer.util.logging.Logger;

import java.io.IOException;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Dungeoneer extends Game {
    public static final Logger LOGGER = new Logger(Dungeoneer.class.getSimpleName());
    private final Client client;
    private final Listener.QueuedListener listener;

    public Dungeoneer() {
        client = new Client(256 * 1024, 256 * 1024, new CreoSerialization());
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

    @Override
    public void create() {
        setScreen(new LoadingScreen());

        PacketSerializer.registerDefault(client.getKryo());

        client.addListener(listener);
        client.start();

        try {
            client.connect(5000, "localhost", 54555, 54777);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Dungeoneer.LOGGER.info("Client initialized.");
    }
}
