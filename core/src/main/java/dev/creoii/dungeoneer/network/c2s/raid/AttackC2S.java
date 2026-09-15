package dev.creoii.dungeoneer.network.c2s.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.List;

public record AttackC2S(long raidId, long accountId, List<Long> clientIds, float mouseDirX, float mouseDirY) {
    public static void write(Output output, AttackC2S o) {
        output.writeLong(o.raidId);
        output.writeLong(o.accountId);

        int size = o.clientIds.size();
        output.writeInt(size, true);
        for (int i = 0; i < size; ++i) {
            output.writeLong(o.clientIds.get(i));
        }

        output.writeFloat(o.mouseDirX);
        output.writeFloat(o.mouseDirY);
    }

    public static AttackC2S read(Input input) {
        long raidId = input.readLong();
        long accountId = input.readLong();

        int size = input.readInt(true);
        List<Long> clientIds = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            clientIds.add(input.readLong());
        }

        return new AttackC2S(raidId, accountId, clientIds, input.readFloat(), input.readFloat());
    }
}
