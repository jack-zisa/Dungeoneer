package dev.creoii.dungeoneer.database;

import dev.creoii.dungeoneer.database.repository.AccountRepository;
import dev.creoii.dungeoneer.database.repository.SessionRepository;
import dev.creoii.dungeoneer.util.logging.Logger;
import org.jdbi.v3.core.Jdbi;

public class Database {
    public static final Logger LOGGER = new Logger(Database.class.getSimpleName());
    private final Jdbi jdbi;
    private final SessionRepository sessions;
    private final AccountRepository accounts;

    public Database() {
        jdbi = Jdbi.create("jdbc:sqlite:dungeoneer.db");
        sessions = new SessionRepository(jdbi);
        accounts = new AccountRepository(jdbi);
        LOGGER.info("Database initialized");
    }

    public AccountRepository getAccounts() {
        return accounts;
    }

    public SessionRepository getSessions() {
        return sessions;
    }
}
