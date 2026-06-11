package dev.creoii.dungeoneer.network;

import com.esotericsoftware.kryonet.Connection;

import java.util.concurrent.ConcurrentLinkedQueue;

public record NetworkQueue(ConcurrentLinkedQueue<QueuedPacket> queue) {
    public NetworkQueue() {
        this(new ConcurrentLinkedQueue<>());
    }

    public void queuePacket(Connection connection, Object o) {
        queue.add(new QueuedPacket(connection, o));
    }

    public record QueuedPacket(Connection connection, Object data) {}
}
