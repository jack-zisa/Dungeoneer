package dev.creoii.dungeoneer.network.s2c.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.util.PacketUtils;
import dev.creoii.dungeoneer.util.RemovalReason;

public record LeaveRaidS2C(long raidId, long accountId, RemovalReason reason) {
    public static void write(Output output, LeaveRaidS2C o) {
        output.writeLong(o.raidId);
        output.writeLong(o.accountId);
        PacketUtils.writeEnum(output, o.reason());
    }

    public static LeaveRaidS2C read(Input input) {
        return new LeaveRaidS2C(input.readLong(), input.readLong(), PacketUtils.readEnum(RemovalReason.class, input));
    }
}
