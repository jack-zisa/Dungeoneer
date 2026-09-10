package dev.creoii.dungeoneer.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import dev.creoii.dungeoneer.util.Tickable;

public abstract class NetworkHandler implements Listener, Tickable {
    private final NetworkQueue networkQueue;

    public NetworkHandler() {
        networkQueue = new NetworkQueue();
    }

    @Override
    public void tick(float dt) {
        NetworkQueue.QueuedPacket packet;
        while ((packet = networkQueue.queue().poll()) != null && PacketSerializer.INSTANCE.isValidPacket(packet.data())) {
            handlePacket(packet.connection(), packet.data());
        }
    }

    @Override
    public void received(Connection connection, Object object) {
        networkQueue.queuePacket(connection, object);
    }

    public abstract void handlePacket(Connection connection, Object object);
}
