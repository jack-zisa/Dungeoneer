package dev.creoii.dungeoneer.database.repository;

import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.util.NetworkUtils;
import org.jdbi.v3.core.Jdbi;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public class AccountRepository {
    private final Jdbi jdbi;

    public AccountRepository(Jdbi jdbi) {
        this.jdbi = jdbi;

        // Initialize schema
        jdbi.useHandle(handle ->
            handle.execute("""
            CREATE TABLE IF NOT EXISTS accounts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username VARCHAR(32) UNIQUE NOT NULL,
                password_hash VARCHAR(128) NOT NULL,
                characters TEXT,
                faction_id INTEGER,
                faction_join_date DATETIME,
                settings TEXT
            )
        """)
        );
    }

    @Nullable
    public Account getByUsername(String username) {
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                SELECT *
                FROM accounts
                WHERE username = :username
            """)
                .bind("username", username)
                .map((rs, _) -> new Account(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    NetworkUtils.parseIds(rs.getString("characters")),
                    rs.getInt("faction_id"),
                    rs.getString("faction_join_date"),
                    rs.getString("settings")
                ))
                .findOne()
                .orElse(null)
        );
    }

    @Nullable
    public Account getById(long id) {
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                SELECT *
                FROM accounts
                WHERE id = :id
            """)
                .bind("id", id)
                .map((rs, _) -> new Account(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    NetworkUtils.parseIds(rs.getString("characters")),
                    rs.getInt("faction_id"),
                    rs.getString("faction_join_date"),
                    rs.getString("settings")
                ))
                .findOne()
                .orElse(null)
        );
    }

    public void updateCharacters(Account account) {
        jdbi.useHandle(handle ->
            handle.createUpdate("""
            UPDATE accounts
            SET characters = :characters
            WHERE id = :id
        """)
                .bind("id", account.id())
                .bind("characters", NetworkUtils.compressIds(account.characters()))
                .execute()
        );
    }

    public void updateFaction(Account account, long factionId) {
        jdbi.useHandle(handle ->
            handle.createUpdate("""
            UPDATE accounts
            SET faction_id = :faction_id,
                faction_join_date = :faction_join_date
            WHERE id = :id
        """)
                .bind("id", account.id())
                .bind("faction_id", factionId)
                .bind("faction_join_date", factionId == -1 ? "" : LocalDateTime.now().toString())
                .execute()
        );
    }

    public void updateSettings(long accountId, String settings) {
        jdbi.useHandle(handle ->
            handle.createUpdate("""
            UPDATE accounts
            SET settings = :settings
            WHERE id = :id
        """)
                .bind("id", accountId)
                .bind("settings", settings)
                .execute()
        );
    }

    public Account create(String username, String passwordHash) {
        long id = jdbi.withHandle(handle ->
            handle.createUpdate("""
                INSERT INTO accounts(username, password_hash)
                VALUES(:username, :password_hash)
            """)
            .bind("username", username)
            .bind("password_hash", passwordHash)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .one()
        );

        return new Account(id, username, passwordHash);
    }
}
