package dev.creoii.dungeoneer.server.database.repository;

import dev.creoii.dungeoneer.definitions.DungeonMapDefinition;
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
                template_id STRING NOT NULL,
                tileset_id STRING NOT NULL,
                last_edit_date DATETIME NOT NULL
            )
        """)
        );
    }

    @Nullable
    public DungeonMapDefinition getByAccountId(long accountId) {
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                        SELECT *
                        FROM dungeon_maps
                        WHERE account_id = :id
                    """)
                .bind("id", accountId)
                .map((rs, _) -> {
                    String lastEditDate = rs.getString("last_edit_date");
                    return new DungeonMapDefinition(
                        rs.getInt("id"),
                        rs.getInt("account_id"),
                        rs.getString("template_id"),
                        rs.getString("tileset_id"),
                        lastEditDate == null || lastEditDate.isBlank() ? null : LocalDateTime.parse(lastEditDate)
                    );
                })
                .findOne()
                .orElse(null)
        );
    }

    public void updateDungeonMap(long accountId, String templateId, String tilesetId) {
        jdbi.useHandle(handle ->
            handle.createUpdate("""
            UPDATE dungeon_maps
            SET template_id = :template_id,
                tileset_id = :tileset_id
            WHERE account_id = :id
        """)
                .bind("id", accountId)
                .bind("template_id", templateId)
                .bind("tileset_id", tilesetId)
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

    public DungeonMapDefinition create(long accountId, String templateId, String tilesetId, LocalDateTime lastEditDate) {
        long id = jdbi.withHandle(handle ->
            handle.createUpdate("""
                INSERT INTO dungeon_maps(account_id, template_id, tileset_id, last_edit_date)
                VALUES(:account_id, :template_id, :tileset_id, :last_edit_date)
            """)
            .bind("account_id", accountId)
            .bind("template_id", templateId)
            .bind("tileset_id", tilesetId)
            .bind("last_edit_date", lastEditDate.toString())
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .one()
        );
        return new DungeonMapDefinition(id, accountId, templateId, tilesetId, lastEditDate);
    }
}
