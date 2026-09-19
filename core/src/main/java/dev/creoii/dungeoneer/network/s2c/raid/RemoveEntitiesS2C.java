package dev.creoii.dungeoneer.network.s2c.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.List;

public record RemoveEntitiesS2C(List<Long> entityIds) {
    public static void write(Output output, RemoveEntitiesS2C o) {
        output.writeInt(o.entityIds.size(), true);
        for (long l : o.entityIds()) {
            output.writeLong(l);
        }
    }

    public static RemoveEntitiesS2C read(Input input) {
        int size = input.readInt(true);
        List<Long> entries = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            entries.add(input.readLong());
        }
        return new RemoveEntitiesS2C(entries);
    }
}
