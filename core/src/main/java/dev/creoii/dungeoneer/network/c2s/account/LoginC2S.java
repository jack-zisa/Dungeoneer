package dev.creoii.dungeoneer.network.c2s.account;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record LoginC2S(String username, String password) {
    public static void write(Output output, LoginC2S o) {
        output.writeString(o.username);
        output.writeString(o.password);
    }

    public static LoginC2S read(Input input) {
        return new LoginC2S(input.readString(), input.readString());
    }
}
