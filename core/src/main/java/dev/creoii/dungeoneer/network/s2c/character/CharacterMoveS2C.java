package dev.creoii.dungeoneer.network.s2c.character;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record CharacterMoveS2C(long characterId, float x, float y) {
    public static void write(Output output, CharacterMoveS2C o) {
        output.writeLong(o.characterId);
        output.writeFloat(o.x);
        output.writeFloat(o.y);
    }

    public static CharacterMoveS2C read(Input input) {
        return new CharacterMoveS2C(input.readLong(), input.readFloat(), input.readFloat());
    }
}
