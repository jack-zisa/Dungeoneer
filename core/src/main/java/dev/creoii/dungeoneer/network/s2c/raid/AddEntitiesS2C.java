package dev.creoii.dungeoneer.network.s2c.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.network.data.BulletPacketData;
import dev.creoii.dungeoneer.network.data.EntityPacketData;

import java.util.ArrayList;
import java.util.List;

public record AddEntitiesS2C(List<Entry> entries) {
    public static void write(Output output, AddEntitiesS2C o) {
        output.writeInt(o.entries.size(), true);
        for (Entry entry : o.entries()) {
            Entry.write(output, entry);
        }
    }

    public static AddEntitiesS2C read(Input input) {
        int size = input.readInt(true);
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            entries.add(Entry.read(input));
        }
        return new AddEntitiesS2C(entries);
    }

    public record Entry(long entityId, float x, float y, EntityPacketData data) {
        public static void write(Output output, Entry o) {
            output.writeLong(o.entityId);
            output.writeFloat(o.x);
            output.writeFloat(o.y);
            output.writeInt(o.data.type().ordinal());
            o.data.write(output);
        }

        public static Entry read(Input input) {
            return new Entry(input.readLong(), input.readFloat(), input.readFloat(), switch (EntityPacketData.Type.values()[input.readInt()]) {
                case BULLET -> BulletPacketData.read(input);
            });
        }
    }
}
