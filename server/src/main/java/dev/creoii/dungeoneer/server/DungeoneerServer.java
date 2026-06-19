package dev.creoii.dungeoneer.server;

import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.network.s2c.character.CharacterMoveS2C;
import dev.creoii.dungeoneer.server.database.Database;
import dev.creoii.dungeoneer.network.CreoSerialization;
import dev.creoii.dungeoneer.server.game.ServerRaid;
import dev.creoii.dungeoneer.server.network.ServerNetworkHandler;
import dev.creoii.dungeoneer.util.logging.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

public class DungeoneerServer {
    public static final int DEFAULT_TCP_PORT = 54556;
    public static final int DEFAULT_UDP_PORT = 54778;
    public static final Logger LOGGER = new Logger(DungeoneerServer.class.getSimpleName());
    private static final float DT = 1f / 20f;
    private final Server server;
    private final ServerNetworkHandler networkHandler;
    private final Database database;
    private final SessionManager sessionManager;
    private final ServerState state;
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
        DataManager.setDebug(isDebug());
        DataManager.load();
        sessionManager = new SessionManager(this);
        state = new ServerState(this);

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

    public ServerState getState() {
        return state;
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
                long start = System.currentTimeMillis();

                Iterator<ServerRaid> iterator = state.getRaids().values().iterator();
                while (iterator.hasNext()) {
                    ServerRaid raid = iterator.next();

                    int connectionId = getSessionManager().getAccountConnections().getOrDefault(raid.getCharacter().get().accountId(), -1);
                    if (connectionId == -1) {
                        iterator.remove();
                        continue;
                    }

                    raid.tick(DT);

                    if (raid.getCharacter().isMoving()) {
                        get().sendToUDP(connectionId, new CharacterMoveS2C(raid.getCharacter().get().id(), raid.getCharacter().getX(), raid.getCharacter().getY()));
                    }
                }

                networkHandler.tick(DT);

                long sleep = tickRate - (System.currentTimeMillis() - start);

                if (sleep > 0) {
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        running = false;
                        break;
                    }
                }
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
