package dev.creoii.dungeoneer.server.database.repository;

import dev.creoii.dungeoneer.server.database.Database;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.definitions.RaidDefinition;
import org.jdbi.v3.core.Jdbi;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public class RaidRepository {
    private final Database database;
    private final Jdbi jdbi;

    public RaidRepository(Database database, Jdbi jdbi) {
        this.database = database;
        this.jdbi = jdbi;

        // Initialize schema
        jdbi.useHandle(handle ->
            handle.execute("""
            CREATE TABLE IF NOT EXISTS raids (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                attacker_id INTEGER NOT NULL,
                target_id INTEGER NOT NULL,
                start_time DATETIME NOT NULL,
                end_time DATETIME
            )
        """)
        );
    }

    public void updateEndTime(long raidId, LocalDateTime endTime) {
        jdbi.useHandle(handle ->
            handle.createUpdate("""
            UPDATE raids
            SET end_time = :end_time
            WHERE id = :id
        """)
                .bind("id", raidId)
                .bind("end_time", endTime.toString())
                .execute()
        );
    }

    @Nullable
    public RaidDefinition getById(long id) {
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                SELECT *
                FROM raids
                WHERE id = :id
            """)
                .bind("id", id)
                .map((rs, _) -> {
                    String endTime = rs.getString("end_time");
                    return new RaidDefinition(
                        rs.getInt("id"),
                        database.getAccounts().getById(rs.getInt("attacker_id")),
                        database.getAccounts().getById(rs.getInt("target_id")),
                        LocalDateTime.parse(rs.getString("start_time")),
                        endTime == null || endTime.isBlank() ? null : LocalDateTime.parse(endTime)
                    );
                })
                .findOne()
                .orElse(null)
        );
    }

    public RaidDefinition create(Account attacker, Account target, LocalDateTime startTime) {
        long id = jdbi.withHandle(handle ->
            handle.createUpdate("""
                INSERT INTO raids(attacker_id, target_id, start_time)
                VALUES(:attacker_id, :target_id, :start_time)
            """)
                .bind("attacker_id", attacker.id())
                .bind("target_id", target.id())
                .bind("start_time", startTime.toString())
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .one()
        );

        return new RaidDefinition(id, attacker, target, startTime, null);
    }
}
