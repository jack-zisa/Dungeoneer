package dev.creoii.dungeoneer.network.c2s.character;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record CharacterMoveStartC2S(long raidId, long characterId, boolean axis, boolean positive) {
    public static void write(Output output, CharacterMoveStartC2S o) {
        output.writeLong(o.raidId);
        output.writeLong(o.characterId);
        output.writeBoolean(o.axis);
        output.writeBoolean(o.positive);
    }

    public static CharacterMoveStartC2S read(Input input) {
        return new CharacterMoveStartC2S(input.readLong(), input.readLong(), input.readBoolean(), input.readBoolean());
    }
}
