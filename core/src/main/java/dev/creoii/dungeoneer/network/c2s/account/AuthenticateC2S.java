package dev.creoii.dungeoneer.network.c2s.account;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record AuthenticateC2S() {
    public static void write(Output output, AuthenticateC2S o) {
    }

    public static AuthenticateC2S read(Input input) {
        return new AuthenticateC2S();
    }
}
