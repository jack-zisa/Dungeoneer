package dev.creoii.dungeoneer.network.s2c.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.RaidDefinition;
import dev.creoii.dungeoneer.util.PacketUtils;

public record SendRaidTargetS2C(RaidDefinition raid, String templateId, String tilesetId) {
    public static void write(Output output, SendRaidTargetS2C o) {
        PacketUtils.writeRaid(output, o.raid);
        output.writeString(o.templateId);
        output.writeString(o.tilesetId);
    }

    public static SendRaidTargetS2C read(Input input) {
        RaidDefinition raid = PacketUtils.readRaid(input);
        return new SendRaidTargetS2C(raid, input.readString(), input.readString());
    }
}
