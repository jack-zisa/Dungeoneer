package dev.creoii.dungeoneer.server;

import com.esotericsoftware.kryonet.Connection;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import dev.creoii.dungeoneer.server.database.definitions.ClientSession;
import dev.creoii.dungeoneer.server.database.definitions.ServerSession;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private final DungeoneerServer server;
    private final ServerSession session;
    private final Map<Integer, ClientSession> connectionSessions;
    private final Map<Long, ClientSession> accountSessions;
    private final BiMap<Long, Integer> accountConnections;

    public SessionManager(DungeoneerServer server) {
        this.server = server;
        connectionSessions = new HashMap<>();
        accountSessions = new HashMap<>();
        accountConnections = HashBiMap.create();
        session = server.getDatabase().getServerSessions().create(LocalDateTime.now());
        DungeoneerServer.LOGGER.info("Server session started");
    }

    public Map<Integer, ClientSession> getConnectionSessions() {
        return connectionSessions;
    }

    public Map<Long, ClientSession> getAccountSessions() {
        return accountSessions;
    }

    public BiMap<Long, Integer> getAccountConnections() {
        return accountConnections;
    }

    public boolean hasSession(int connectionId) {
        return connectionSessions.containsKey(connectionId);
    }

    @Nullable
    public ClientSession startClientSession(Connection connection, long accountId) {
        if (!accountSessions.containsKey(accountId) && !connectionSessions.containsKey(connection.getID())) {
            ClientSession clientSession = server.getDatabase().getClientSessions().create(accountId, LocalDateTime.now());
            connectionSessions.put(connection.getID(), clientSession);
            accountSessions.put(accountId, clientSession);
            accountConnections.put(accountId, connection.getID());
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
            accountConnections.remove(clientSession.accountId());

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
