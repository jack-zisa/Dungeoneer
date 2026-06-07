package dev.creoii.dungeoneer.server;

import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import dev.creoii.dungeoneer.server.database.Database;
import dev.creoii.dungeoneer.network.CreoSerialization;
import dev.creoii.dungeoneer.server.network.ServerNetworkHandler;
import dev.creoii.dungeoneer.util.logging.Logger;

import java.io.IOException;
import java.util.Set;

public class DungeoneerServer {
    public static final int DEFAULT_TCP_PORT = 54556;
    public static final int DEFAULT_UDP_PORT = 54778;
    private final Server server;
    private final ServerNetworkHandler networkHandler;
    private final Database database;
    private final SessionManager sessionManager;
    public static final Logger LOGGER = new Logger(DungeoneerServer.class.getSimpleName());
    private volatile Status status;
    private volatile boolean running = true;
    private final Thread gameThread;
    private final ServerProperties properties;
    private final ServerSecrets secrets;
    private final boolean debug;

    public DungeoneerServer(ServerProperties properties, ServerSecrets secrets, boolean debug) throws IOException {
        this.properties = properties;
        this.secrets = secrets;
        this.debug = debug;

        Log.NONE();

        setStatus(Status.STARTING);

        LOGGER.info("Server starting on ports: TCP %s | UDP %s", properties.tcpPort(), properties.udpPort());

        server = new Server(256 * 1024, 256 * 1024, new CreoSerialization());
        server.start();
        server.bind(properties.tcpPort(), properties.udpPort());

        networkHandler = new ServerNetworkHandler(this);
        database = new Database();
        sessionManager = new SessionManager(this);

        setStatus(Status.PAUSED);

        gameThread = new Thread(this::run, "Game");
        gameThread.start();

        if (debug) {
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
            LOGGER.info("Active Threads:");
            threadSet.forEach(thread -> LOGGER.info("    " + thread.getName()));
        }
    }

    public Server get() {
        return server;
    }

    public Database getDatabase() {
        return database;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public ServerProperties getProperties() {
        return properties;
    }

    public ServerSecrets getSecrets() {
        return secrets;
    }

    public boolean isDebug() {
        return debug;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        LOGGER.info("Server status set to: %s", status.name());

        if (status == Status.PAUSED) {
            properties.write(properties.runDirectory());
            secrets.write(properties.runDirectory());
        }
    }

    public void run() {
        final long tickRate = 1000L / 20L; // 20 TPS

        while (running) {
            if (status.shouldTick()) {
                networkHandler.tick();
            }

            try {
                Thread.sleep(tickRate);
            } catch (InterruptedException e) {
                running = false;
                break;
            }
        }

        finish();
    }

    public void stop() {
        setStatus(Status.STOPPING);
        running = false;
        gameThread.interrupt();
    }

    public void finish() {
        setStatus(Status.FINISHED);

        properties.write(properties.runDirectory());
        secrets.write(properties.runDirectory());

        sessionManager.endSessions();
        server.close();
    }

    public enum Status {
        STARTING(false),    // Server is setting up
        PAUSED(false),      // Server is set up & ready for clients to connect
        RUNNING(true),      // Server has clients connected
        STOPPING(false),    // Server is shutting down
        FINISHED(false);    // Server has stopped

        private final boolean shouldTick;

        Status(boolean shouldTick) {
            this.shouldTick = shouldTick;
        }

        public boolean shouldTick() {
            return shouldTick;
        }
    }
}
