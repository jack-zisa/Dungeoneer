package dev.creoii.dungeoneer.network.c2s.character;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record SelectActiveCharacterC2S(long accountId, long activeCharacterId) {
    public static void write(Output output, SelectActiveCharacterC2S o) {
        output.writeLong(o.accountId);
        output.writeLong(o.activeCharacterId);
    }

    public static SelectActiveCharacterC2S read(Input input) {
        return new SelectActiveCharacterC2S(input.readLong(), input.readLong());
    }
}
