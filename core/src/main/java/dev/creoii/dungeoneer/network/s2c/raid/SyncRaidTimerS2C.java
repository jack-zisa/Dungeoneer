package dev.creoii.dungeoneer.network.s2c.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record SyncRaidTimerS2C(long timeRemaining) {
    public static void write(Output output, SyncRaidTimerS2C o) {
        output.writeLong(o.timeRemaining);
    }

    public static SyncRaidTimerS2C read(Input input) {
        return new SyncRaidTimerS2C(input.readLong());
    }
}
