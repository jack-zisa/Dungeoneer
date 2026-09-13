package dev.creoii.dungeoneer.server.database.repository;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.CharacterClass;
import dev.creoii.dungeoneer.definitions.item.inventory.EquipmentInventory;
import dev.creoii.dungeoneer.definitions.item.inventory.Inventory;
import dev.creoii.dungeoneer.definitions.item.inventory.Slot;
import dev.creoii.dungeoneer.server.database.Database;
import dev.creoii.dungeoneer.util.NetworkUtils;
import org.jdbi.v3.core.Jdbi;
import org.jspecify.annotations.Nullable;

import java.io.Reader;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
                equipment TEXT NOT NULL,
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
                        parseInventory(rs.getString("equipment")),
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
                        parseInventory(rs.getString("equipment")),
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

    public void updateEquipment(long accountId, long id, EquipmentInventory equipment) {
        jdbi.useHandle(handle ->
            handle.createUpdate("""
            UPDATE characters
            SET equipment = :equipment
            WHERE id = :id AND account_id = :account_id
        """)
                .bind("id", id)
                .bind("account_id", accountId)
                .bind("equipment", writeInventory(equipment))
                .execute()
        );
    }

    public CharacterDefinition create(Account account, CharacterClass characterClass, Inventory inventory) {
        long id = jdbi.withHandle(handle ->
            handle.createUpdate("""
                INSERT INTO characters(account_id, class, equipment)
                VALUES(:account_id, :class, :equipment)
            """)
            .bind("account_id", account.id())
            .bind("class", characterClass.id())
            .bind("equipment", writeInventory(inventory))
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .one()
        );

        return new CharacterDefinition(id, account.id(), DataManager.getCharacterClass(characterClass.id()), inventory, null);
    }

    public static Inventory parseInventory(String s) {
        Reader reader = new StringReader(s);
        JsonElement equipmentJson = Database.GSON.fromJson(reader, JsonElement.class);
        return Inventory.DB_CODEC.parse(JsonOps.INSTANCE, equipmentJson).getOrThrow();
    }

    public static String writeInventory(Inventory inventory) {
        List<Long> items = new ArrayList<>(inventory.size());
        for (Slot slot : inventory) {
            if (slot.isEmpty()) {
                items.add(-1L);
            } else items.add(DataManager.getInternalId(DataManager.SchemaType.ITEM, slot.getItem().id()));
        }
        return Database.GSON.toJson(items);
    }}
