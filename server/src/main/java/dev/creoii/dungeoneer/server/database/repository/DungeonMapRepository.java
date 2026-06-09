package dev.creoii.dungeoneer.server.database.repository;

import dev.creoii.dungeoneer.definitions.DungeonMap;
import org.jdbi.v3.core.Jdbi;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public class DungeonMapRepository {
    private final Jdbi jdbi;

    public DungeonMapRepository(Jdbi jdbi) {
        this.jdbi = jdbi;

        // Initialize schema
        jdbi.useHandle(handle ->
            handle.execute("""
            CREATE TABLE IF NOT EXISTS dungeon_maps (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                account_id INTEGER UNIQUE NOT NULL,
                map_data BLOB,
                last_edit_date DATETIME NOT NULL
            )
        """)
        );
    }

    @Nullable
    public DungeonMap getByAccountId(long accountId) {
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                        SELECT *
                        FROM dungeon_maps
                        WHERE account_id = :id
                    """)
                .bind("id", accountId)
                .map((rs, _) -> {
                    String lastEditDate = rs.getString("last_edit_date");
                    return new DungeonMap(
                        rs.getInt("id"),
                        rs.getInt("account_id"),
                        rs.getBytes("map_data"),
                        lastEditDate == null || lastEditDate.isBlank() ? null : LocalDateTime.parse(lastEditDate)
                    );
                })
                .findOne()
                .orElse(null)
        );
    }

    public void updateDungeonMap(long accountId, byte[] blob) {
        jdbi.useHandle(handle ->
            handle.createUpdate("""
            UPDATE dungeon_maps
            SET map_data = :map_data
            WHERE account_id = :id
        """)
                .bind("id", accountId)
                .bind("map_data", blob)
                .execute()
        );
    }

    public void updateLastEditDate(long accountId, LocalDateTime last_edit_date) {
        jdbi.useHandle(handle ->
            handle.createUpdate("""
            UPDATE dungeon_maps
            SET last_edit_date = :last_edit_date
            WHERE account_id = :account_id
        """)
                .bind("account_id", accountId)
                .bind("last_edit_date", last_edit_date.toString())
                .execute()
        );
    }

    public DungeonMap create(long accountId, byte[] mapData, LocalDateTime lastEditDate) {
        long id = jdbi.withHandle(handle ->
            handle.createUpdate("""
                INSERT INTO dungeon_maps(account_id, map_data, last_edit_date)
                VALUES(:account_id, :map_data, :last_edit_date)
            """)
            .bind("account_id", accountId)
            .bind("map_data", mapData)
            .bind("last_edit_date", lastEditDate.toString())
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .one()
        );
        return new DungeonMap(id, accountId, mapData, lastEditDate);
    }
}
