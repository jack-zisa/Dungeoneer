package dev.creoii.dungeoneer.server.database.repository;

import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.server.database.Database;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.definitions.RaidDefinition;
import dev.creoii.dungeoneer.util.NetworkUtils;
import org.jdbi.v3.core.Jdbi;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                attacker_ids STRING NOT NULL,
                character_ids STRING NOT NULL,
                target_id INTEGER NOT NULL,
                required_characters INTEGER NOT NULL,
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

    public void updateAttackers(RaidDefinition raidDefinition) {
        jdbi.useHandle(handle ->
            handle.createUpdate("""
            UPDATE raids
            SET attacker_ids = :attacker_ids,
                character_ids = :character_ids
            WHERE id = :id
        """)
                .bind("id", raidDefinition.id())
                .bind("attacker_ids", NetworkUtils.compressIds(raidDefinition.attackers().stream().map(Account::id).collect(Collectors.toList())))
                .bind("character_ids", NetworkUtils.compressIds(raidDefinition.characters().stream().map(CharacterDefinition::id).collect(Collectors.toList())))
                .execute()
        );
    }

    public List<RaidDefinition> getAvailableRaids(int requiredCharacters) {
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                SELECT *
                FROM raids
                WHERE required_characters = :requiredCharacters
                  AND end_time IS NULL
                """)
                .bind("requiredCharacters", requiredCharacters)
                .map((rs, _) -> {
                    String endTime = rs.getString("end_time");
                    List<Account> attackerIds = database.getAccounts().getByIds(NetworkUtils.parseIds(rs.getString("attacker_ids")));
                    List<CharacterDefinition> characterIds = database.getCharacters().getByIds(NetworkUtils.parseIds(rs.getString("character_ids")));
                    return new RaidDefinition(
                        rs.getInt("id"),
                        attackerIds,
                        characterIds,
                        database.getAccounts().getById(rs.getInt("target_id")),
                        rs.getInt("required_characters"),
                        LocalDateTime.parse(rs.getString("start_time")),
                        endTime == null || endTime.isBlank() ? null : LocalDateTime.parse(endTime)
                    );
                })
                .list()
                .stream()
                .filter(raid -> raid.attackers().size() < raid.requiredCharacters())
                .collect(Collectors.toList())
        );
    }

    public void delete(long id) {
        jdbi.withHandle(handle ->
            handle.createUpdate("""
                DELETE FROM raids
                WHERE id = :id
                """)
                .bind("id", id)
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
                    List<Account> attackerIds = database.getAccounts().getByIds(NetworkUtils.parseIds(rs.getString("attacker_ids")));
                    List<CharacterDefinition> characterIds = database.getCharacters().getByIds(NetworkUtils.parseIds(rs.getString("character_ids")));
                    return new RaidDefinition(
                        rs.getInt("id"),
                        attackerIds,
                        characterIds,
                        database.getAccounts().getById(rs.getInt("target_id")),
                        rs.getInt("required_characters"),
                        LocalDateTime.parse(rs.getString("start_time")),
                        endTime == null || endTime.isBlank() ? null : LocalDateTime.parse(endTime)
                    );
                })
                .findOne()
                .orElse(null)
        );
    }

    public RaidDefinition create(Account attacker, CharacterDefinition character, Account target, int requiredCharacters, LocalDateTime startTime) {
        long id = jdbi.withHandle(handle ->
            handle.createUpdate("""
                INSERT INTO raids(attacker_ids, character_ids, target_id, required_characters, start_time)
                VALUES(:attacker_ids, :character_ids, :target_id, :required_characters, :start_time)
            """)
                .bind("attacker_ids", attacker.id())
                .bind("character_ids", attacker.activeCharacterId())
                .bind("target_id", target.id())
                .bind("required_characters", requiredCharacters)
                .bind("start_time", startTime.toString())
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .one()
        );

        List<Account> attackers = new ArrayList<>();
        attackers.add(attacker);
        List<CharacterDefinition> characters = new ArrayList<>();
        characters.add(character);
        return new RaidDefinition(id, attackers, characters, target, requiredCharacters, startTime, null);
    }
}
