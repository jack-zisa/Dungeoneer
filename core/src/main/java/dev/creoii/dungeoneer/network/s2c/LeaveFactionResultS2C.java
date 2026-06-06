package dev.creoii.dungeoneer.network.s2c;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.network.PacketResult;

public record LeaveFactionResultS2C(PacketResult result) {
    public static void write(Output output, LeaveFactionResultS2C o) {
        output.writeInt(o.result.ordinal());
    }

    public static LeaveFactionResultS2C read(Input input) {
        return new LeaveFactionResultS2C(PacketResult.values()[input.readInt()]);
    }
}
