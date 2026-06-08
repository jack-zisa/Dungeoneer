package dev.creoii.dungeoneer.server.database.repository;

import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.definitions.Faction;
import dev.creoii.dungeoneer.server.database.Database;
import dev.creoii.dungeoneer.util.NetworkUtils;
import org.jdbi.v3.core.Jdbi;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class FactionRepository {
    private final Database database;
    private final Jdbi jdbi;

    public FactionRepository(Database database, Jdbi jdbi) {
        this.database = database;
        this.jdbi = jdbi;

        // Initialize schema
        jdbi.useHandle(handle ->
            handle.execute("""
            CREATE TABLE IF NOT EXISTS factions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name VARCHAR(32) NOT NULL,
                description VARCHAR(255) NOT NULL,
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
                    rs.getString("description"),
                    NetworkUtils.parseIds(rs.getString("accounts")).stream().map(database.getAccounts()::getById).collect(Collectors.toList()),
                    new LinkedHashMap<>()
                ))
                .findOne()
                .orElse(null)
        );
    }

    public List<Faction> search(String search) {
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                SELECT *
                FROM factions
                WHERE LOWER(name) like LOWER(:search)
            """)
                .bind("search", "%" + search + "%")
                .map((rs, _) -> new Faction(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    NetworkUtils.parseIds(rs.getString("accounts")).stream().map(database.getAccounts()::getById).collect(Collectors.toList()),
                    new LinkedHashMap<>()
                ))
                .list()
        );
    }

    public void updateAccounts(Faction faction) {
        jdbi.useHandle(handle ->
            handle.createUpdate("""
            UPDATE factions
            SET accounts = :accounts
            WHERE id = :id
        """)
                .bind("id", faction.id())
                .bind("accounts", NetworkUtils.compressIds(faction.accounts().stream().map(Account::id).collect(Collectors.toList())))
                .execute()
        );
    }

    public Faction create(long accountId, String name, String description) {
        long id = jdbi.withHandle(handle ->
            handle.createUpdate("""
                INSERT INTO factions(name, description, accounts)
                VALUES(:name, :description, :accounts)
            """)
                .bind("name", name)
                .bind("description", description)
                .bind("accounts", accountId)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .one()
        );

        List<Account> accounts = new ArrayList<>();
        accounts.add(database.getAccounts().getById(accountId));
        return new Faction(id, name, description, accounts, new LinkedHashMap<>());
    }
}
