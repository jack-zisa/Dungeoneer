package dev.creoii.dungeoneer.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.password4j.Password;
import dev.creoii.dungeoneer.DungeoneerServer;
import dev.creoii.dungeoneer.database.Database;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.database.definitions.ClientSession;
import dev.creoii.dungeoneer.definitions.Character;
import dev.creoii.dungeoneer.definitions.CharacterClass;
import dev.creoii.dungeoneer.network.c2s.account.CreateCharacterC2S;
import dev.creoii.dungeoneer.network.c2s.account.LoginC2S;
import dev.creoii.dungeoneer.network.c2s.account.RequestCharactersC2S;
import dev.creoii.dungeoneer.network.c2s.account.RequestLoginC2S;
import dev.creoii.dungeoneer.network.s2c.account.AuthenticateS2C;
import dev.creoii.dungeoneer.network.s2c.account.CreateCharacterResultS2C;
import dev.creoii.dungeoneer.network.s2c.account.LoginResultS2C;
import dev.creoii.dungeoneer.network.s2c.account.SendCharactersS2C;
import dev.creoii.dungeoneer.util.Tickable;

public class ServerNetworkHandler implements Listener, Tickable {
    private final DungeoneerServer server;
    private final ServerNetworkQueue networkQueue;

    public ServerNetworkHandler(DungeoneerServer server) {
        this.server = server;
        networkQueue = new ServerNetworkQueue();
        server.get().addListener(this);

        PacketSerializer.registerDefault(server.get().getKryo());
    }

    @Override
    public void tick() {
        ServerNetworkQueue.QueuedPacket packet;
        while ((packet = networkQueue.queue().poll()) != null && PacketSerializer.INSTANCE.isValidPacket(packet.data())) {
            handlePacket(packet.connection(), packet.data());
        }
    }

    @Override
    public void connected(Connection connection) {
        DungeoneerServer.LOGGER.info("Client connected: %s", connection);

        if (server.getStatus() == DungeoneerServer.Status.PAUSED && !server.get().getConnections().isEmpty()) {
            server.setStatus(DungeoneerServer.Status.RUNNING);
        }
    }

    @Override
    public void disconnected(Connection connection) {
        DungeoneerServer.LOGGER.info("Client disconnected: %s", connection);

        server.getSessionManager().endClientSession(connection);

        if (server.getStatus() == DungeoneerServer.Status.RUNNING && server.get().getConnections().isEmpty()) {
            server.setStatus(DungeoneerServer.Status.PAUSED);
        }
    }

    @Override
    public void received(Connection connection, Object object) {
        networkQueue.queuePacket(connection, object);
    }

    public void handlePacket(Connection connection, Object object) {
        if (server.isDebug())
            DungeoneerServer.LOGGER.debug("%s | Connection %s | %s", connection.getRemoteAddressTCP(), connection.getID(), object.getClass().getSimpleName());

        if (object instanceof RequestLoginC2S) {
            server.get().sendToUDP(connection.getID(), new AuthenticateS2C());
        } else if (object instanceof LoginC2S(String username, String password)) {
            Account account = server.getDatabase().getAccounts().getByUsername(username);
            if (account == null) {
                // TODO: Implement password requirements
                account = server.getDatabase().getAccounts().create(username, Password.hash(password + server.getSecrets().pepper()).withArgon2().getResult());
                Database.LOGGER.info("Created account: %s", account.username());
            } else if (Password.check(password + server.getSecrets().pepper(), account.passwordHash()).withArgon2()) {
                Database.LOGGER.info("Loaded account: %s", account.username());
            } else {
                DungeoneerServer.LOGGER.error("Failed login for account: %s", account.username());
                server.get().sendToUDP(connection.getID(), new LoginResultS2C(LoginResultS2C.Result.FAIL, null));
                return;
            }

            ClientSession clientSession = server.getSessionManager().startClientSession(connection, account.id());
            if (clientSession != null) {
                server.get().sendToUDP(connection.getID(), new LoginResultS2C(LoginResultS2C.Result.SUCCESS, account));
            } else {
                connection.close();
                server.get().sendToUDP(connection.getID(), new LoginResultS2C(LoginResultS2C.Result.FAIL, null));
            }
        } else if (object instanceof RequestCharactersC2S) {
            ClientSession clientSession = server.getSessionManager().getConnectionSessions().get(connection.getID());
            if (clientSession == null) return;

            Account account = server.getDatabase().getAccounts().getById(clientSession.accountId());
            if (account == null) return;

            server.get().sendToUDP(connection.getID(), new SendCharactersS2C(account.characters().stream().map(integer -> server.getDatabase().getCharacters().getById(integer)).toList()));
        } else if (object instanceof CreateCharacterC2S(long accountId, CharacterClass characterClass)) {
            Account account = server.getDatabase().getAccounts().getById(accountId);
            if (account != null) {
                Character character = server.getDatabase().getCharacters().create(account, characterClass);
                if (character != null) {
                    account.characters().add(character.id());
                    server.getDatabase().getAccounts().updateCharacters(account);

                    DungeoneerServer.LOGGER.info("Created character of class '%s' for account: %s", characterClass.id(), accountId);
                    server.get().sendToUDP(connection.getID(), new CreateCharacterResultS2C(LoginResultS2C.Result.SUCCESS, character));
                    return;
                }
            }

            server.get().sendToUDP(connection.getID(), new CreateCharacterResultS2C(LoginResultS2C.Result.FAIL, null));
        }
    }
}
