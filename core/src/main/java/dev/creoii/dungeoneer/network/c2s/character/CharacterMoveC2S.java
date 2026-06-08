package dev.creoii.dungeoneer.network.c2s.character;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record CharacterMoveC2S(long raidId, long characterId, int movementFlags) {
    public static void write(Output output, CharacterMoveC2S o) {
        output.writeLong(o.raidId);
        output.writeLong(o.characterId);
        output.writeInt(o.movementFlags);
    }

    public static CharacterMoveC2S read(Input input) {
        return new CharacterMoveC2S(input.readLong(), input.readLong(), input.readInt());
    }
}
