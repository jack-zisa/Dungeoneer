package dev.creoii.dungeoneer.network.data;

import com.esotericsoftware.kryo.io.Output;

public interface EntityPacketData {
    Type type();

    void write(Output output);

    enum Type {
        BULLET,
        BULLET_GROUP
    }
}
