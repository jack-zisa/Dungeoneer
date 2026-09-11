package dev.creoii.dungeoneer.network.c2s.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.util.PacketUtils;
import dev.creoii.dungeoneer.util.RemovalReason;

public record LeaveRaidC2S(long raidId, long accountId, RemovalReason reason) {
    public static void write(Output output, LeaveRaidC2S o) {
        output.writeLong(o.raidId);
        output.writeLong(o.accountId);
        PacketUtils.writeEnum(output, o.reason());
    }

    public static LeaveRaidC2S read(Input input) {
        return new LeaveRaidC2S(input.readLong(), input.readLong(), PacketUtils.readEnum(RemovalReason.class, input));
    }
}
