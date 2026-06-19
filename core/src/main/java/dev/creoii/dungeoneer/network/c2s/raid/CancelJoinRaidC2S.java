package dev.creoii.dungeoneer.network.c2s.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record CancelJoinRaidC2S(long accountId, long raidId) {
    public static void write(Output output, CancelJoinRaidC2S o) {
        output.writeLong(o.accountId);
        output.writeLong(o.raidId);
    }

    public static CancelJoinRaidC2S read(Input input) {
        return new CancelJoinRaidC2S(input.readLong(), input.readLong());
    }
}
