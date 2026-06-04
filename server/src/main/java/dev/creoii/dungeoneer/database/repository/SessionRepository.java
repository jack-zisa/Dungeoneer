package dev.creoii.dungeoneer.database.repository;

import dev.creoii.dungeoneer.database.definitions.Account;
import dev.creoii.dungeoneer.database.definitions.Session;
import org.jdbi.v3.core.Jdbi;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public class SessionRepository {
    private final Jdbi jdbi;

    public SessionRepository(Jdbi jdbi) {
        this.jdbi = jdbi;

        // Initialize schema
        jdbi.useHandle(handle ->
            handle.execute("""
            CREATE TABLE IF NOT EXISTS sessions (
                id BIGSERIAL PRIMARY KEY,
                account_id BIGSERIAL,
                start_time DATETIME NOT NULL,
                end_time DATETIME
            )
        """)
        );
    }

    public void updateEndTime(long sessionId, LocalDateTime endTime) {
        jdbi.useHandle(handle ->
            handle.createUpdate("""
            UPDATE sessions
            SET end_time = :end_time
            WHERE id = :id
        """)
                .bind("end_time", endTime)
                .bind("id", sessionId)
                .execute()
        );
    }

    @Nullable
    public Session getCurrentSession(long accountId) {
        return jdbi.withHandle(handle ->
            handle.createQuery("""
            SELECT *
            FROM sessions
            WHERE account_id = :account_id
              AND end_time IS NULL
            ORDER BY start_time DESC
            LIMIT 1
        """)
                .bind("account_id", accountId)
                .map((rs, _) -> new Session(
                    rs.getLong("id"),
                    rs.getLong("account_id"),
                    rs.getObject("start_time", LocalDateTime.class),
                    rs.getObject("end_time", LocalDateTime.class)
                ))
                .findOne()
                .orElse(null)
        );
    }

    @Nullable
    public Session getById(long id) {
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                SELECT *
                FROM sessions
                WHERE id = :id
            """)
                .bind("id", id)
                .map((rs, _) -> new Session(
                    rs.getLong("id"),
                    rs.getLong("account_id"),
                    LocalDateTime.parse(rs.getString("start_time")),
                    LocalDateTime.parse(rs.getString("end_time"))
                ))
                .findOne()
                .orElse(null)
        );
    }

    public Session create(long accountId, LocalDateTime startTime) {
        long id = jdbi.withHandle(handle ->
            handle.createUpdate("""
                INSERT INTO sessions(account_id, start_time)
                VALUES(:account_id, :start_time)
            """)
                .bind("account_id", accountId)
                .bind("start_time", startTime)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .one()
        );

        return new Session(id, accountId, startTime, null);
    }
}
