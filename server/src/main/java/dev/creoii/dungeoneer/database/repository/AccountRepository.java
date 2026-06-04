package dev.creoii.dungeoneer.database.repository;

import dev.creoii.dungeoneer.database.definitions.Account;
import org.jdbi.v3.core.Jdbi;
import org.jspecify.annotations.Nullable;

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
                password_hash VARCHAR(128) NOT NULL
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
                    rs.getString("password_hash")
                ))
                .findOne()
                .orElse(null)
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
