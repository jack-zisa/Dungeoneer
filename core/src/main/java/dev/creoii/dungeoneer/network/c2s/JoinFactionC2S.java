package dev.creoii.dungeoneer.network.c2s;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record JoinFactionC2S(long accountId, String factionName) {
    public static void write(Output output, JoinFactionC2S o) {
        output.writeLong(o.accountId);
        output.writeString(o.factionName);
    }

    public static JoinFactionC2S read(Input input) {
        return new JoinFactionC2S(input.readLong(), input.readString());
    }
}
