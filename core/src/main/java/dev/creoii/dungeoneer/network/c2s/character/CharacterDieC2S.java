package dev.creoii.dungeoneer.network.c2s.character;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record CharacterDieC2S(long raidId, long characterId) {
    public static void write(Output output, CharacterDieC2S o) {
        output.writeLong(o.raidId);
        output.writeLong(o.characterId);
    }

    public static CharacterDieC2S read(Input input) {
        return new CharacterDieC2S(input.readLong(), input.readLong());
    }
}
