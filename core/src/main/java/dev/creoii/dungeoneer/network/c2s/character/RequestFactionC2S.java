package dev.creoii.dungeoneer.network.c2s.character;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record RequestFactionC2S(long accountId) {
    public static void write(Output output, RequestFactionC2S o) {
        output.writeLong(o.accountId);
    }

    public static RequestFactionC2S read(Input input) {
        return new RequestFactionC2S(input.readLong());
    }
}
