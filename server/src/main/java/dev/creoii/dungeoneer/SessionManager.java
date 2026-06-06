package dev.creoii.dungeoneer;

import com.esotericsoftware.kryonet.Connection;
import dev.creoii.dungeoneer.database.definitions.ClientSession;
import dev.creoii.dungeoneer.database.definitions.ServerSession;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private final DungeoneerServer server;
    private final ServerSession session;
    private final Map<Integer, ClientSession> connectionSessions;
    private final Map<Long, ClientSession> accountSessions;

    public SessionManager(DungeoneerServer server) {
        this.server = server;
        this.connectionSessions = new HashMap<>();
        this.accountSessions = new HashMap<>();
        session = server.getDatabase().getServerSessions().create(LocalDateTime.now());
        DungeoneerServer.LOGGER.info("Server session started");
    }

    public ServerSession getSession() {
        return session;
    }

    @Nullable
    public ClientSession startClientSession(Connection connection, long accountId) {
        if (!accountSessions.containsKey(accountId) && !connectionSessions.containsKey(connection.getID())) {
            ClientSession clientSession = server.getDatabase().getClientSessions().create(accountId, LocalDateTime.now());
            connectionSessions.put(connection.getID(), clientSession);
            accountSessions.put(accountId, clientSession);
            DungeoneerServer.LOGGER.info("Client session started for account id: %s", accountId);
            return clientSession;
        } else {
            DungeoneerServer.LOGGER.error("Client session or connection already active for account id: %s", accountId);
            return null;
        }
    }

    public void endClientSession(Connection connection) {
        ClientSession clientSession = connectionSessions.remove(connection.getID());
        if (clientSession != null) {
            accountSessions.remove(clientSession.accountId());
            server.getDatabase().getClientSessions().updateEndTime(clientSession.id(), LocalDateTime.now());
            DungeoneerServer.LOGGER.info("Client session ended for account id: %s", clientSession.accountId());
        }
    }

    public void endSessions() {
        for (Connection connection : server.get().getConnections()) {
            if (connection.isConnected()) {
                endClientSession(connection);
            }
        }

        server.getDatabase().getServerSessions().updateEndTime(session.id(), LocalDateTime.now());
        DungeoneerServer.LOGGER.info("Server session ended");
    }
}
