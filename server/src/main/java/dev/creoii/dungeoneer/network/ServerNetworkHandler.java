package dev.creoii.dungeoneer.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import dev.creoii.dungeoneer.DungeoneerServer;
import dev.creoii.dungeoneer.database.Database;
import dev.creoii.dungeoneer.database.definitions.Account;
import dev.creoii.dungeoneer.database.definitions.Session;
import dev.creoii.dungeoneer.network.c2s.account.LoginC2S;
import dev.creoii.dungeoneer.network.c2s.account.RequestLoginC2S;
import dev.creoii.dungeoneer.network.s2c.account.AllowLoginS2C;
import dev.creoii.dungeoneer.network.s2c.account.LoginResultS2C;
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
        DungeoneerServer.LOGGER.info("Client connected: " + connection);

        if (server.getStatus() == DungeoneerServer.Status.PAUSED && !server.get().getConnections().isEmpty()) {
            server.setStatus(DungeoneerServer.Status.RUNNING);
        }
    }

    @Override
    public void disconnected(Connection connection) {
        DungeoneerServer.LOGGER.info("Client disconnected: " + connection);

        server.getSessionManager().endSession(connection);

        if (server.getStatus() == DungeoneerServer.Status.RUNNING && server.get().getConnections().isEmpty()) {
            server.setStatus(DungeoneerServer.Status.PAUSED);
        }
    }

    @Override
    public void received(Connection connection, Object object) {
        networkQueue.queuePacket(connection, object);
    }

    public void handlePacket(Connection connection, Object object) {
        if (server.getProperties().debug())
            DungeoneerServer.LOGGER.debug(connection.getRemoteAddressTCP() + " | Connection " + connection.getID() + " | " + object.getClass().getSimpleName());

        if (object instanceof RequestLoginC2S) {
            server.get().sendToUDP(connection.getID(), new AllowLoginS2C());
        } else if (object instanceof LoginC2S(String username, String password)) {
            Account account = server.getDatabase().getAccounts().getByUsername(username);
            if (account == null) {
                account = server.getDatabase().getAccounts().create(username, password);
                Database.LOGGER.info("Created account: " + account.username());
            } else Database.LOGGER.info("Loaded account: " + account.username());

            Session session = server.getSessionManager().startSession(connection, account.id());
            if (session != null) {
                server.get().sendToUDP(connection.getID(), new LoginResultS2C(LoginResultS2C.Result.SUCCESS));
            } else {
                connection.close();
                server.get().sendToUDP(connection.getID(), new LoginResultS2C(LoginResultS2C.Result.FAIL));
            }
        }
    }
}
