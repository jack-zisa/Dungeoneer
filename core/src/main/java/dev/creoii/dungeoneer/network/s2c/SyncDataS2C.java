package dev.creoii.dungeoneer.network.s2c;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.IOException;

public record SyncDataS2C(byte[] data) {
    public static void write(Output output, SyncDataS2C o) {
        output.writeBytes(o.data);
    }

    public static SyncDataS2C read(Input input) {
        byte[] bytes;
        try {
            bytes = input.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new SyncDataS2C(bytes);
    }
}
