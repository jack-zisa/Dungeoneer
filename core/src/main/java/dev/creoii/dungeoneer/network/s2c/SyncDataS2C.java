package dev.creoii.dungeoneer.network.s2c;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.IOException;

public record SyncDataS2C(String schema, byte[] data) {
    public static void write(Output output, SyncDataS2C o) {
        output.writeString(o.schema);
        output.writeBytes(o.data);
    }

    public static SyncDataS2C read(Input input) {
        String schema = input.readString();
        byte[] bytes;
        try {
            bytes = input.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new SyncDataS2C(schema, bytes);
    }
}
