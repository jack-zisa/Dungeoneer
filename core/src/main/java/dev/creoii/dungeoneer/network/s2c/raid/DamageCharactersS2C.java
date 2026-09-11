package dev.creoii.dungeoneer.network.s2c.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.List;

public record DamageCharactersS2C(List<Entry> entries) {
    public static void write(Output output, DamageCharactersS2C o) {
        output.writeInt(o.entries.size());
        o.entries.forEach(entry -> {
            output.writeLong(entry.accountId);
            output.writeInt(entry.damage);
        });
    }

    public static DamageCharactersS2C read(Input input) {
        int size = input.readInt();
        List<Entry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            entries.add(new Entry(input.readLong(), input.readInt()));
        }
        return new DamageCharactersS2C(entries);
    }

    public record Entry(long accountId, int damage) {
    }
}
