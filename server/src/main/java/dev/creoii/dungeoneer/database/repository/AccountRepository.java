package dev.creoii.dungeoneer.database.repository;

import dev.creoii.dungeoneer.database.definitions.Account;
import org.jdbi.v3.core.Jdbi;

public class AccountRepository {
    private final Jdbi jdbi;

    public AccountRepository(Jdbi jdbi) {
        this.jdbi = jdbi;

        // Initialize schema
        jdbi.useHandle(handle ->
            handle.execute("""
            CREATE TABLE IF NOT EXISTS accounts (
                id BIGSERIAL PRIMARY KEY,
                username VARCHAR(32) UNIQUE NOT NULL,
                password VARCHAR(128) NOT NULL
            )
        """)
        );
    }

    public Account findByUsername(String username) {
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                SELECT *
                FROM accounts
                WHERE username = :username
            """)
            .bind("username", username)
            .map((rs, ctx) -> new Account(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password")
            ))
            .findOne()
            .orElse(null)
        );
    }

    public Account create(String username, String password) {
        long id = jdbi.withHandle(handle ->
            handle.createUpdate("""
                INSERT INTO accounts(username, password)
                VALUES(:username, :password)
            """)
            .bind("username", username)
            .bind("password", password)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .one()
        );

        return new Account(id, username, password);
    }
}
