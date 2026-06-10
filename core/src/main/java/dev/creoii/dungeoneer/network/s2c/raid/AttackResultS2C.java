package dev.creoii.dungeoneer.network.s2c.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.network.PacketResult;

public record AttackResultS2C(PacketResult result) {
    public static void write(Output output, AttackResultS2C o) {
        output.writeInt(o.result.ordinal());
    }

    public static AttackResultS2C read(Input input) {
        return new AttackResultS2C(PacketResult.values()[input.readInt()]);
    }
}
