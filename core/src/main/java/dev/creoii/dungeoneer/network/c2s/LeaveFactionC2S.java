package dev.creoii.dungeoneer.network.c2s;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record LeaveFactionC2S(long accountId) {
    public static void write(Output output, LeaveFactionC2S o) {
        output.writeLong(o.accountId);
    }

    public static LeaveFactionC2S read(Input input) {
        return new LeaveFactionC2S(input.readLong());
    }
}
