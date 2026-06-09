package dev.creoii.dungeoneer.server.database.repository;

import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.definitions.Character;
import dev.creoii.dungeoneer.definitions.CharacterClass;
import org.jdbi.v3.core.Jdbi;
import org.jspecify.annotations.Nullable;

public class CharacterRepository {
    private final Jdbi jdbi;

    public CharacterRepository(Jdbi jdbi) {
        this.jdbi = jdbi;

        // Initialize schema
        jdbi.useHandle(handle ->
            handle.execute("""
            CREATE TABLE IF NOT EXISTS characters (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                account_id INTEGER NOT NULL,
                class VARCHAR(32) NOT NULL
            )
        """)
        );
    }

    @Nullable
    public Character getById(long id) {
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                SELECT *
                FROM characters
                WHERE id = :id
            """)
                .bind("id", id)
                .map((rs, _) -> new Character(
                    rs.getInt("id"),
                    rs.getInt("account_id"),
                    DataManager.getCharacterClass(rs.getString("class"))
                ))
                .findOne()
                .orElse(null)
        );
    }

    public Character create(Account account, CharacterClass characterClass) {
        long id = jdbi.withHandle(handle ->
            handle.createUpdate("""
                INSERT INTO characters(account_id, class)
                VALUES(:account_id, :class)
            """)
            .bind("account_id", account.id())
            .bind("class", characterClass.id())
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .one()
        );

        return new Character(id, account.id(), DataManager.getCharacterClass(characterClass.id()));
    }
}
