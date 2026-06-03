package dev.creoii.dungeoneer.network.s2c.account;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record AllowLoginS2C() {
    public static void write(Output output, AllowLoginS2C o) {
    }

    public static AllowLoginS2C read(Input input) {
        return new AllowLoginS2C();
    }
}
