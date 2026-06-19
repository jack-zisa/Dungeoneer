package dev.creoii.dungeoneer.network.s2c.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.RaidDefinition;
import dev.creoii.dungeoneer.util.PacketUtils;

public record SendRaidS2C(RaidDefinition raid, byte[] mapData) {
    public static void write(Output output, SendRaidS2C o) {
        PacketUtils.writeRaid(output, o.raid);
        output.writeInt(o.mapData.length);
        output.writeBytes(o.mapData);
    }

    public static SendRaidS2C read(Input input) {
        RaidDefinition raid = PacketUtils.readRaid(input);
        int len = input.readInt();
        return new SendRaidS2C(raid, input.readBytes(len));
    }
}
