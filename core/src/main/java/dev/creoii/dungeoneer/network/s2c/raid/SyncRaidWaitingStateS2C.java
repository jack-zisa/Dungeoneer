package dev.creoii.dungeoneer.network.s2c.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.RaidDefinition;
import dev.creoii.dungeoneer.util.PacketUtils;

public record SyncRaidWaitingStateS2C(RaidDefinition raidDefinition) {
    public static void write(Output output, SyncRaidWaitingStateS2C o) {
        PacketUtils.writeRaid(output, o.raidDefinition);
    }

    public static SyncRaidWaitingStateS2C read(Input input) {
        return new SyncRaidWaitingStateS2C(PacketUtils.readRaid(input));
    }
}
