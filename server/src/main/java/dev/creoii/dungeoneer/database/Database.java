package dev.creoii.dungeoneer.database;

import dev.creoii.dungeoneer.database.repository.AccountRepository;
import org.jdbi.v3.core.Jdbi;

public class Database {
    private final Jdbi jdbi;
    private final AccountRepository accounts;

    public Database() {
        jdbi = Jdbi.create("jdbc:sqlite:dungeoneer.db");
        accounts = new AccountRepository(jdbi);
    }

    public AccountRepository getAccounts() {
        return accounts;
    }
}
