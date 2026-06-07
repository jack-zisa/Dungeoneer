package dev.creoii.dungeoneer.server.database;

import dev.creoii.dungeoneer.server.database.repository.*;
import dev.creoii.dungeoneer.server.database.repository.*;
import dev.creoii.dungeoneer.util.logging.Logger;
import org.jdbi.v3.core.Jdbi;

public class Database {
    public static final Logger LOGGER = new Logger(Database.class.getSimpleName());
    private final Jdbi jdbi;
    private final ServerSessionRepository serverSessions;
    private final ClientSessionRepository clientSessions;
    private final FactionRepository factions;
    private final AccountRepository accounts;
    private final CharacterRepository characters;
    private final RaidRepository raids;

    public Database() {
        jdbi = Jdbi.create("jdbc:sqlite:dungeoneer.db");

        clientSessions = new ClientSessionRepository(jdbi);
        serverSessions = new ServerSessionRepository(jdbi);
        factions = new FactionRepository(this, jdbi);
        accounts = new AccountRepository(jdbi);
        characters = new CharacterRepository(jdbi);
        raids = new RaidRepository(this, jdbi);
        LOGGER.info("Database initialized.");
    }

    public ServerSessionRepository getServerSessions() {
        return serverSessions;
    }

    public ClientSessionRepository getClientSessions() {
        return clientSessions;
    }

    public FactionRepository getFactions() {
        return factions;
    }

    public AccountRepository getAccounts() {
        return accounts;
    }

    public CharacterRepository getCharacters() {
        return characters;
    }

    public RaidRepository getRaids() {
        return raids;
    }
}
