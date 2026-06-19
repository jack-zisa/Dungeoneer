package dev.creoii.dungeoneer.network.s2c.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record BulletMoveS2C(Entry[] entries) {
    public static void write(Output output, BulletMoveS2C o) {
        output.writeInt(o.entries.length);
        for (Entry entry : o.entries) {
            output.writeInt(entry.bulletId);
            output.writeFloat(entry.x);
            output.writeFloat(entry.y);
        }
    }

    public static BulletMoveS2C read(Input input) {
        int len = input.readInt();
        Entry[] entries = new Entry[len];
        for (int i = 0; i < len; ++i) {
            entries[i] = new Entry(input.readInt(), input.readFloat(), input.readFloat());
        }
        return new BulletMoveS2C(entries);
    }

    public record Entry(int bulletId, float x, float y) {
        public static void write(Output output, Entry o) {
            output.writeInt(o.bulletId);
            output.writeFloat(o.x);
            output.writeFloat(o.y);
        }

        public static Entry read(Input input) {
            return new Entry(input.readInt(), input.readFloat(), input.readFloat());
        }
    }
}
