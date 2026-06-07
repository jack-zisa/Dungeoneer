package dev.creoii.dungeoneer.network.c2s.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record EndRaidC2S(long raidId) {
    public static void write(Output output, EndRaidC2S o) {
        output.writeLong(o.raidId);
    }

    public static EndRaidC2S read(Input input) {
        return new EndRaidC2S(input.readLong());
    }
}
