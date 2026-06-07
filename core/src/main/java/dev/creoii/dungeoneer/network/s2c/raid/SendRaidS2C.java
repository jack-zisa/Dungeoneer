package dev.creoii.dungeoneer.network.s2c.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Raid;
import dev.creoii.dungeoneer.util.PacketUtils;

public record SendRaidS2C(Raid raid) {
    public static void write(Output output, SendRaidS2C o) {
        PacketUtils.writeRaid(output, o.raid);
    }

    public static SendRaidS2C read(Input input) {
        return new SendRaidS2C(PacketUtils.readRaid(input));
    }
}
