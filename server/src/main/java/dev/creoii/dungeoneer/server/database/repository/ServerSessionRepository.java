package dev.creoii.dungeoneer.server.database.repository;

import dev.creoii.dungeoneer.server.database.definitions.ServerSession;
import org.jdbi.v3.core.Jdbi;

import java.time.LocalDateTime;

public class ServerSessionRepository {
    private final Jdbi jdbi;

    public ServerSessionRepository(Jdbi jdbi) {
        this.jdbi = jdbi;

        // Initialize schema
        jdbi.useHandle(handle ->
            handle.execute("""
            CREATE TABLE IF NOT EXISTS server_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                start_time DATETIME NOT NULL,
                end_time DATETIME
            )
        """)
        );
    }

    public void updateEndTime(long sessionId, LocalDateTime endTime) {
        jdbi.useHandle(handle ->
            handle.createUpdate("""
            UPDATE server_sessions
            SET end_time = :end_time
            WHERE id = :id
        """)
                .bind("end_time", endTime.toString())
                .bind("id", sessionId)
                .execute()
        );
    }

    public ServerSession create(LocalDateTime startTime) {
        long id = jdbi.withHandle(handle ->
            handle.createUpdate("""
                INSERT INTO server_sessions(start_time)
                VALUES(:start_time)
            """)
                .bind("start_time", startTime.toString())
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .one()
        );

        return new ServerSession(id, startTime, null);
    }
}
