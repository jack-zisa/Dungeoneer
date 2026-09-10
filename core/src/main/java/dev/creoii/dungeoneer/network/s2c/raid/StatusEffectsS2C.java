package dev.creoii.dungeoneer.network.s2c.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.List;

public record StatusEffectsS2C(List<Entry> entries) {
    public static void write(Output output, StatusEffectsS2C o) {
        output.writeInt(o.entries.size());
        o.entries.forEach(entry -> {
            output.writeLong(entry.accountId);
            output.writeLong(entry.add);
            output.writeLong(entry.remove);
        });
    }

    public static StatusEffectsS2C read(Input input) {
        int size = input.readInt();
        List<Entry> entries1 = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            entries1.add(new Entry(input.readLong(), input.readLong(), input.readLong()));
        }
        return new StatusEffectsS2C(entries1);
    }

    public record Entry(long accountId, long add, long remove) {
    }
}
