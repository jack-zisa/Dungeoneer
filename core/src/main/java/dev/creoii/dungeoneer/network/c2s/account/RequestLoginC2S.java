package dev.creoii.dungeoneer.network.c2s.account;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record RequestLoginC2S() {
    public static void write(Output output, RequestLoginC2S o) {
    }

    public static RequestLoginC2S read(Input input) {
        return new RequestLoginC2S();
    }
}
