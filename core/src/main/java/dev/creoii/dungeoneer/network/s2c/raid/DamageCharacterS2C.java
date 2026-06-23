package dev.creoii.dungeoneer.network.s2c.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record DamageCharacterS2C(long accountId, int damage) {
    public static void write(Output output, DamageCharacterS2C o) {
        output.writeLong(o.accountId);
        output.writeInt(o.damage);
    }

    public static DamageCharacterS2C read(Input input) {
        return new DamageCharacterS2C(input.readLong(), input.readInt());
    }
}
