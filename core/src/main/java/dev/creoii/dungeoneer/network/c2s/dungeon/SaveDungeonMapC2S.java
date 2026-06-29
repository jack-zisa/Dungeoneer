package dev.creoii.dungeoneer.network.c2s.dungeon;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record SaveDungeonMapC2S(long accountId, String templateId, String tilesetId) {
    public static void write(Output output, SaveDungeonMapC2S o) {
        output.writeLong(o.accountId);
        output.writeString(o.templateId);
        output.writeString(o.tilesetId);
    }

    public static SaveDungeonMapC2S read(Input input) {
        long accountId = input.readLong();
        return new SaveDungeonMapC2S(accountId, input.readString(), input.readString());
    }
}
