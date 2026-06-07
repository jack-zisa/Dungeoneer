package dev.creoii.dungeoneer.network.c2s;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record RequestCharactersC2S(long accountId) {
    public static void write(Output output, RequestCharactersC2S o) {
        output.writeLong(o.accountId);
    }

    public static RequestCharactersC2S read(Input input) {
        return new RequestCharactersC2S(input.readLong());
    }
}
