package dev.creoii.dungeoneer.server.database.repository;

import dev.creoii.dungeoneer.server.database.definitions.ClientSession;
import org.jdbi.v3.core.Jdbi;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public class ClientSessionRepository {
    private final Jdbi jdbi;

    public ClientSessionRepository(Jdbi jdbi) {
        this.jdbi = jdbi;

        // Initialize schema
        jdbi.useHandle(handle ->
            handle.execute("""
            CREATE TABLE IF NOT EXISTS client_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                account_id INTEGER,
                start_time DATETIME NOT NULL,
                end_time DATETIME
            )
        """)
        );
    }

    public void updateEndTime(long sessionId, LocalDateTime endTime) {
        jdbi.useHandle(handle ->
            handle.createUpdate("""
            UPDATE client_sessions
            SET end_time = :end_time
            WHERE id = :id
        """)
                .bind("id", sessionId)
                .bind("end_time", endTime.toString())
                .execute()
        );
    }

    @Nullable
    public ClientSession getCurrentSession(long accountId) {
        return jdbi.withHandle(handle ->
            handle.createQuery("""
            SELECT *
            FROM client_sessions
            WHERE account_id = :account_id
              AND end_time IS NULL
            ORDER BY start_time DESC
            LIMIT 1
        """)
                .bind("account_id", accountId)
                .map((rs, _) -> {
                    String endTime = rs.getString("end_time");
                    return new ClientSession(
                        rs.getInt("id"),
                        rs.getInt("account_id"),
                        LocalDateTime.parse(rs.getString("start_time")),
                        endTime == null || endTime.isBlank() ? null : LocalDateTime.parse(endTime)
                    );
                })
                .findOne()
                .orElse(null)
        );
    }

    @Nullable
    public ClientSession getById(long id) {
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                SELECT *
                FROM client_sessions
                WHERE id = :id
            """)
                .bind("id", id)
                .map((rs, _) -> {
                    String endTime = rs.getString("end_time");
                    return new ClientSession(
                        rs.getInt("id"),
                        rs.getInt("account_id"),
                        LocalDateTime.parse(rs.getString("start_time")),
                        endTime == null || endTime.isBlank() ? null : LocalDateTime.parse(endTime)
                    );
                })
                .findOne()
                .orElse(null)
        );
    }

    public ClientSession create(long accountId, LocalDateTime startTime) {
        long id = jdbi.withHandle(handle ->
            handle.createUpdate("""
                INSERT INTO client_sessions(account_id, start_time)
                VALUES(:account_id, :start_time)
            """)
                .bind("account_id", accountId)
                .bind("start_time", startTime.toString())
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .one()
        );

        return new ClientSession(id, accountId, startTime, null);
    }
}
