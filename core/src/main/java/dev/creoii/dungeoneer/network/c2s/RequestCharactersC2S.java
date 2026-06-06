package dev.creoii.dungeoneer.network.c2s;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record RequestCharactersC2S() {
    public static final RequestCharactersC2S INSTANCE = new RequestCharactersC2S();

    public static void write(Output output, RequestCharactersC2S o) {
    }

    public static RequestCharactersC2S read(Input input) {
        return INSTANCE;
    }
}
