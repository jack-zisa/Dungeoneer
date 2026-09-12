package dev.creoii.dungeoneer.network.s2c.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.List;

public record MoveCharactersS2C(List<Entry> entries) {
    public static void write(Output output, MoveCharactersS2C o) {
        output.writeInt(o.entries.size(), true);
        for (Entry entry : o.entries()) {
            Entry.write(output, entry);
        }
    }

    public static MoveCharactersS2C read(Input input) {
        int size = input.readInt(true);
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            entries.add(new Entry(input.readLong(), input.readLong(), input.readFloat(), input.readFloat()));
        }
        return new MoveCharactersS2C(entries);
    }

    public record Entry(long accountId, long characterId, float x, float y) {
        public static void write(Output output, Entry o) {
            output.writeLong(o.accountId);
            output.writeLong(o.characterId);
            output.writeFloat(o.x);
            output.writeFloat(o.y);
        }

        public static Entry read(Input input) {
            return new Entry(input.readLong(), input.readLong(), input.readFloat(), input.readFloat());
        }
    }
}
