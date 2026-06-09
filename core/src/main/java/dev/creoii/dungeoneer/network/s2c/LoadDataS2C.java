package dev.creoii.dungeoneer.network.s2c;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record LoadDataS2C() {
    private static final LoadDataS2C INSTANCE = new LoadDataS2C();

    public static void write(Output output, LoadDataS2C o) {
    }

    public static LoadDataS2C read(Input input) {
        return INSTANCE;
    }
}
