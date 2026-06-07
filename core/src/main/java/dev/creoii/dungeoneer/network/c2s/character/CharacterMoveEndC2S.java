package dev.creoii.dungeoneer.network.c2s.character;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record CharacterMoveEndC2S(long raidId, long characterId, boolean axis, boolean positive) {
    public static void write(Output output, CharacterMoveEndC2S o) {
        output.writeLong(o.raidId);
        output.writeLong(o.characterId);
        output.writeBoolean(o.axis);
        output.writeBoolean(o.positive);
    }

    public static CharacterMoveEndC2S read(Input input) {
        return new CharacterMoveEndC2S(input.readLong(), input.readLong(), input.readBoolean(), input.readBoolean());
    }
}
