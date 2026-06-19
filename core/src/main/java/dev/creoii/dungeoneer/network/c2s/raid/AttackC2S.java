package dev.creoii.dungeoneer.network.c2s.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record AttackC2S(long raidId, long accountId, float mouseDirX, float mouseDirY) {
    public static void write(Output output, AttackC2S o) {
        output.writeLong(o.raidId);
        output.writeLong(o.accountId);
        output.writeFloat(o.mouseDirX);
        output.writeFloat(o.mouseDirY);
    }

    public static AttackC2S read(Input input) {
        return new AttackC2S(input.readLong(), input.readLong(), input.readFloat(), input.readFloat());
    }
}
