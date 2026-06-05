package dev.creoii.dungeoneer.network.s2c.account;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record AuthenticateS2C() {
    public static void write(Output output, AuthenticateS2C o) {
    }

    public static AuthenticateS2C read(Input input) {
        return new AuthenticateS2C();
    }
}
