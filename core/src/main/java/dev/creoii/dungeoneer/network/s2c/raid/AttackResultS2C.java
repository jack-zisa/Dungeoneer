package dev.creoii.dungeoneer.network.s2c.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.network.PacketResult;

import java.util.ArrayList;
import java.util.List;

public record AttackResultS2C(PacketResult result, long clientId, List<Long> entityIds) {
    public static void write(Output output, AttackResultS2C o) {
        output.writeInt(o.result.ordinal());
        if (o.result == PacketResult.SUCCESS) {
            output.writeLong(o.clientId);
            output.writeInt(o.entityIds.size(), true);
            for (long l : o.entityIds) {
                output.writeLong(l);
            }
        }
    }

    public static AttackResultS2C read(Input input) {
        PacketResult result = PacketResult.values()[input.readInt()];
        if (result == PacketResult.SUCCESS) {
            long clientId = input.readLong();
            int size = input.readInt(true);
            List<Long> entityIds = new ArrayList<>();
            for (int i = 0; i < size; ++i) {
                entityIds.add(input.readLong());
            }
            return new AttackResultS2C(result, clientId, entityIds);
        }
        return new AttackResultS2C(result, -1L, List.of());
    }
}
