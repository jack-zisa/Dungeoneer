package dev.creoii.dungeoneer;

import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import dev.creoii.dungeoneer.database.Database;
import dev.creoii.dungeoneer.network.CreoSerialization;
import dev.creoii.dungeoneer.network.ServerNetworkHandler;
import dev.creoii.dungeoneer.util.logging.Logger;

import java.io.IOException;

public class DungeoneerServer {
    private final Server server;
    private final ServerNetworkHandler networkHandler;
    private final Database database;
    public static final Logger LOGGER = new Logger(ServerLauncher.class.getSimpleName());
    private volatile Status status;
    private final Thread gameThread;
    private final ServerProperties properties;

    public DungeoneerServer(ServerProperties properties) throws IOException {
        this.properties = properties;

        Log.NONE();

        setStatus(Status.STARTING);

        server = new Server(256 * 1024, 256 * 1024, new CreoSerialization());
        server.start();
        server.bind(properties.tcpPort(), properties.udpPort());

        LOGGER.info("Server started on ports: TCP " + properties.tcpPort() + " | UDP " + properties.udpPort());

        networkHandler = new ServerNetworkHandler(this);
        database = new Database();

        setStatus(Status.PAUSED);

        gameThread = new Thread(this::run, "Game Loop");
        gameThread.start();
    }

    public Server get() {
        return server;
    }

    public Database getDatabase() {
        return database;
    }

    public Status getStatus() {
        return status;
    }

    public ServerProperties getProperties() {
        return properties;
    }

    public void setStatus(Status status) {
        this.status = status;
        LOGGER.info("Server status set to: " + status.name());
    }

    public void run() {
        final long tickTime = 1000L / 20L; // 20 TPS

        while (true) {
            if (status.shouldTick()) {
                networkHandler.tick();
            }

            try {
                Thread.sleep(tickTime);
            } catch (InterruptedException e) {
                break;
            }
        }

        server.close();
    }

    public enum Status {
        STARTING(false),   // Server is setting up
        PAUSED(false),     // Server is set up & ready for clients to connect
        RUNNING(true);    // Server has clients connected

        private final boolean shouldTick;

        Status(boolean shouldTick) {
            this.shouldTick = shouldTick;
        }

        public boolean shouldTick() {
            return shouldTick;
        }
    }
}
