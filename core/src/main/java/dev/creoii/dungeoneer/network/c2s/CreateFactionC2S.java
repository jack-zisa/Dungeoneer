package dev.creoii.dungeoneer.network.c2s;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record CreateFactionC2S(long accountId, String factionName) {
    public static void write(Output output, CreateFactionC2S o) {
        output.writeLong(o.accountId);
        output.writeString(o.factionName);
    }

    public static CreateFactionC2S read(Input input) {
        return new CreateFactionC2S(input.readLong(), input.readString());
    }
}
