package dev.creoii.dungeoneer.database;

import dev.creoii.dungeoneer.database.repository.AccountRepository;
import dev.creoii.dungeoneer.database.repository.ClientSessionRepository;
import dev.creoii.dungeoneer.database.repository.ServerSessionRepository;
import dev.creoii.dungeoneer.util.logging.Logger;
import org.jdbi.v3.core.Jdbi;

public class Database {
    public static final Logger LOGGER = new Logger(Database.class.getSimpleName());
    private final Jdbi jdbi;
    private final ServerSessionRepository serverSessions;
    private final ClientSessionRepository clientSessions;
    private final AccountRepository accounts;

    public Database() {
        jdbi = Jdbi.create("jdbc:sqlite:dungeoneer.db");
        clientSessions = new ClientSessionRepository(jdbi);
        serverSessions = new ServerSessionRepository(jdbi);
        accounts = new AccountRepository(jdbi);
        LOGGER.info("Database initialized.");
    }

    public AccountRepository getAccounts() {
        return accounts;
    }

    public ServerSessionRepository getServerSessions() {
        return serverSessions;
    }

    public ClientSessionRepository getClientSessions() {
        return clientSessions;
    }
}
