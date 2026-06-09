package dev.creoii.dungeoneer.network.c2s.dungeon;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record SaveDungeonMapC2S(long accountId, byte[] mapData) {
    public static void write(Output output, SaveDungeonMapC2S o) {
        output.writeLong(o.accountId);
        output.writeInt(o.mapData.length);
        output.writeBytes(o.mapData);
    }

    public static SaveDungeonMapC2S read(Input input) {
        long accountId = input.readLong();
        int len = input.readInt();
        return new SaveDungeonMapC2S(accountId, input.readBytes(len));
    }
}
