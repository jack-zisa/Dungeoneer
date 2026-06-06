package dev.creoii.dungeoneer.database.repository;

import dev.creoii.dungeoneer.definitions.Faction;
import dev.creoii.dungeoneer.util.NetworkUtils;
import org.jdbi.v3.core.Jdbi;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FactionRepository {
    private final Jdbi jdbi;

    public FactionRepository(Jdbi jdbi) {
        this.jdbi = jdbi;

        // Initialize schema
        jdbi.useHandle(handle ->
            handle.execute("""
            CREATE TABLE IF NOT EXISTS factions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name VARCHAR(32) NOT NULL,
                accounts TEXT
            )
        """)
        );
    }

    @Nullable
    public Faction getById(long id) {
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                SELECT *
                FROM factions
                WHERE id = :id
            """)
                .bind("id", id)
                .map((rs, _) -> new Faction(
                    rs.getInt("id"),
                    rs.getString("name"),
                    NetworkUtils.parseIds(rs.getString("accounts"))
                ))
                .findOne()
                .orElse(null)
        );
    }

    public void updateAccounts(Faction faction) {
        jdbi.useHandle(handle ->
            handle.createUpdate("""
            UPDATE accounts
            SET characters = :characters
            WHERE id = :id
        """)
                .bind("id", faction.id())
                .bind("accounts", NetworkUtils.compressIds(faction.accounts()))
                .execute()
        );
    }

    public Faction create(long accountId, String name) {
        long id = jdbi.withHandle(handle ->
            handle.createUpdate("""
                INSERT INTO factions(name, accounts)
                VALUES(:name, :accounts)
            """)
            .bind("name", name)
            .bind("accounts", accountId)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .one()
        );

        List<Long> accounts = new ArrayList<>();
        accounts.add(accountId);
        return new Faction(id, name, accounts);
    }
}
