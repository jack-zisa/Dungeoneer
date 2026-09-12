package dev.creoii.dungeoneer.server.database.repository;

import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.CharacterClass;
import org.jdbi.v3.core.Jdbi;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;

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
                class VARCHAR(32) NOT NULL,
                death_date DATETIME
            )
        """)
        );
    }

    @Nullable
    public CharacterDefinition getById(long id) {
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                SELECT *
                FROM characters
                WHERE id = :id
            """)
                .bind("id", id)
                .map((rs, _) -> {
                    String deathDate = rs.getString("death_date");
                    return new CharacterDefinition(
                        rs.getInt("id"),
                        rs.getInt("account_id"),
                        DataManager.getCharacterClass(rs.getString("class")),
                        deathDate == null || deathDate.isBlank() ? null : LocalDateTime.parse(deathDate)
                    );
                })
                .findOne()
                .orElse(null)
        );
    }

    public List<CharacterDefinition> getByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                SELECT *
                FROM characters
                WHERE id IN (<ids>)
                """)
                .bindList("ids", ids)
                .map((rs, _) -> {
                    String deathDate = rs.getString("death_date");
                    return new CharacterDefinition(
                        rs.getInt("id"),
                        rs.getInt("account_id"),
                        DataManager.getCharacterClass(rs.getString("class")),
                        deathDate == null || deathDate.isBlank() ? null : LocalDateTime.parse(deathDate)
                    );
                })
                .list()
        );
    }

    public boolean kill(long id) {
        return jdbi.withHandle(handle ->
            handle.createUpdate("""
            UPDATE characters
               SET death_date = :death_date
             WHERE id = :id
            """)
                .bind("id", id)
                .bind("death_date", LocalDateTime.now().toString())
                .execute()
        ) == 1;
    }

    public CharacterDefinition create(Account account, CharacterClass characterClass) {
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

        return new CharacterDefinition(id, account.id(), DataManager.getCharacterClass(characterClass.id()), null);
    }
}
