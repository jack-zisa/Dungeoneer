package dev.creoii.dungeoneer.network.s2c.character;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.util.PacketUtils;
import dev.creoii.dungeoneer.util.stat.StatContainer;

public record StatUpdatesS2C(long accountId, StatContainer stats) {
    public static void write(Output output, StatUpdatesS2C o) {
        output.writeLong(o.accountId);
        PacketUtils.writeStatContainer(output, o.stats);
    }

    public static StatUpdatesS2C read(Input input) {
        return new StatUpdatesS2C(input.readLong(), PacketUtils.readStatContainer(input));
    }
}
