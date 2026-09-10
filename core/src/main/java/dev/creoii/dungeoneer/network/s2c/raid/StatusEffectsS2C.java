package dev.creoii.dungeoneer.network.s2c.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record StatusEffectsS2C(long accountId, long add, long remove) {
    public static void write(Output output, StatusEffectsS2C o) {
        output.writeLong(o.accountId);
        output.writeLong(o.add);
        output.writeLong(o.remove);
    }

    public static StatusEffectsS2C read(Input input) {
        return new StatusEffectsS2C(input.readLong(), input.readLong(), input.readLong());
    }
}
