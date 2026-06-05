package dev.creoii.dungeoneer;

import com.esotericsoftware.kryonet.Connection;
import dev.creoii.dungeoneer.database.definitions.Session;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private final DungeoneerServer server;
    private final Map<Integer, Session> connectionSessions;
    private final Map<Long, Session> accountSessions;

    public SessionManager(DungeoneerServer server) {
        this.server = server;
        this.connectionSessions = new HashMap<>();
        this.accountSessions = new HashMap<>();
    }

    @Nullable
    public Session startSession(Connection connection, long accountId) {
        if (!accountSessions.containsKey(accountId) && !connectionSessions.containsKey(connection.getID())) {
            Session session = server.getDatabase().getSessions().create(accountId, LocalDateTime.now());
            connectionSessions.put(connection.getID(), session);
            accountSessions.put(accountId, session);
            DungeoneerServer.LOGGER.info("Session started for account id: %s", accountId);
            return session;
        } else {
            DungeoneerServer.LOGGER.error("Session or connection already active for account id: %s", accountId);
            return null;
        }
    }

    public void endSession(Connection connection) {
        Session session = connectionSessions.remove(connection.getID());
        if (session != null) {
            accountSessions.remove(session.accountId());
            server.getDatabase().getSessions().updateEndTime(session.id(), LocalDateTime.now());
            DungeoneerServer.LOGGER.info("Session ended for account id: %s", session.accountId());
        }
    }
}
