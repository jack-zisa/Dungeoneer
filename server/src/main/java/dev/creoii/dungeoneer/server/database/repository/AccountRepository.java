package dev.creoii.dungeoneer.server.database.repository;

import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.util.NetworkUtils;
import org.jdbi.v3.core.Jdbi;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;

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
                gold INTEGER NOT NULL,
                gems INTEGER NOT NULL,
                character_slots INTEGER NOT NULL,
                characters TEXT,
                active_character_id INTEGER,
                faction_id INTEGER,
                faction_join_date DATETIME,
                last_login_date DATETIME NOT NULL
            )
        """)
        );
    }

    public boolean containsId(Long id) {
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                        SELECT 1
                        FROM accounts
                        WHERE id = :id
                    """)
                .bind("id", id)
                .mapTo(Integer.class)
                .findOne()
                .isPresent()
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
                .map((rs, _) -> {
                    String factionJoinDate = rs.getString("faction_join_date");
                    String lastLoginDate = rs.getString("last_login_date");
                    return new Account(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getInt("gold"),
                        rs.getInt("gems"),
                        rs.getInt("character_slots"),
                        NetworkUtils.parseIds(rs.getString("characters")),
                        rs.getInt("active_character_id"),
                        rs.getInt("faction_id"),
                        factionJoinDate == null || factionJoinDate.isBlank() ? null : LocalDateTime.parse(factionJoinDate),
                        lastLoginDate == null || lastLoginDate.isBlank() ? null : LocalDateTime.parse(lastLoginDate)
                    );
                })
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
                .map((rs, _) -> {
                    String factionJoinDate = rs.getString("faction_join_date");
                    String lastLoginDate = rs.getString("last_login_date");
                    return new Account(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getInt("gold"),
                        rs.getInt("gems"),
                        rs.getInt("character_slots"),
                        NetworkUtils.parseIds(rs.getString("characters")),
                        rs.getInt("active_character_id"),
                        rs.getInt("faction_id"),
                        factionJoinDate == null || factionJoinDate.isBlank() ? null : LocalDateTime.parse(factionJoinDate),
                        lastLoginDate == null || lastLoginDate.isBlank() ? null : LocalDateTime.parse(lastLoginDate)
                    );
                })
                .findOne()
                .orElse(null)
        );
    }

    public List<Account> getByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                SELECT *
                FROM accounts
                WHERE id IN (<ids>)
                """)
                .bindList("ids", ids)
                .map((rs, ctx) -> {
                    String factionJoinDate = rs.getString("faction_join_date");
                    String lastLoginDate = rs.getString("last_login_date");
                    return new Account(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getInt("gold"),
                        rs.getInt("gems"),
                        rs.getInt("character_slots"),
                        NetworkUtils.parseIds(rs.getString("characters")),
                        rs.getInt("active_character_id"),
                        rs.getInt("faction_id"),
                        factionJoinDate == null || factionJoinDate.isBlank() ? null : LocalDateTime.parse(factionJoinDate),
                        lastLoginDate == null || lastLoginDate.isBlank() ? null : LocalDateTime.parse(lastLoginDate)
                    );
                })
                .list()
        );
    }

    @Nullable
    public Account getRaidTarget(long excludedId) {
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                        SELECT a.*
                        FROM accounts a
                        LEFT JOIN dungeon_maps d
                            ON a.id = d.account_id
                        WHERE a.id != :exclude
                            AND d.id IS NOT NULL
                        ORDER BY RANDOM()
                        LIMIT 1
                    """)
                .bind("exclude", excludedId)
                .map((rs, _) -> {
                    String factionJoinDate = rs.getString("faction_join_date");
                    String lastLoginDate = rs.getString("last_login_date");
                    return new Account(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getInt("gold"),
                        rs.getInt("gems"),
                        rs.getInt("character_slots"),
                        NetworkUtils.parseIds(rs.getString("characters")),
                        rs.getInt("active_character_id"),
                        rs.getInt("faction_id"),
                        factionJoinDate == null || factionJoinDate.isBlank() ? null : LocalDateTime.parse(factionJoinDate),
                        lastLoginDate == null || lastLoginDate.isBlank() ? null : LocalDateTime.parse(lastLoginDate)
                    );
                })
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

    public void updateActiveCharacter(long accountId, long activeCharacterId) {
        jdbi.useHandle(handle ->
            handle.createUpdate("""
            UPDATE accounts
            SET active_character_id = :active_character_id
            WHERE id = :id
        """)
                .bind("id", accountId)
                .bind("active_character_id", activeCharacterId)
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

    public void updateLastLoginTime(long accountId, LocalDateTime lastLoginTime) {
        jdbi.useHandle(handle ->
            handle.createUpdate("""
            UPDATE accounts
            SET last_login_date = :last_login_date
            WHERE id = :id
        """)
                .bind("id", accountId)
                .bind("last_login_date", lastLoginTime.toString())
                .execute()
        );
    }

    public Account create(String username, String passwordHash, LocalDateTime lastLoginDate) {
        long id = jdbi.withHandle(handle ->
            handle.createUpdate("""
                INSERT INTO accounts(username, password_hash, gold, gems, character_slots, characters, last_login_date)
                VALUES(:username, :password_hash, :gold, :gems, 6, :characters, :last_login_date)
            """)
            .bind("username", username)
            .bind("password_hash", passwordHash)
            .bind("gold", 1000)
            .bind("gems", 100)
            .bind("characters", "-1,-1,-1,-1,-1,-1")
            .bind("last_login_date", lastLoginDate.toString())
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .one()
        );

        return new Account(id, username, passwordHash, 1000, 100, 6, lastLoginDate);
    }
}
