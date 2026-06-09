package dev.creoii.dungeoneer.network.c2s.dungeon;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record RequestDungeonMapC2S(long accountId) {
    public static void write(Output output, RequestDungeonMapC2S o) {
        output.writeLong(o.accountId);
    }

    public static RequestDungeonMapC2S read(Input input) {
        return new RequestDungeonMapC2S(input.readLong());
    }
}
