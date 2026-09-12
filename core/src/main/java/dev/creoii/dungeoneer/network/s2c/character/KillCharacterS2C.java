package dev.creoii.dungeoneer.network.s2c.character;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.util.PacketUtils;

public record KillCharacterS2C(CharacterDefinition character) {
    public static void write(Output output, KillCharacterS2C o) {
        PacketUtils.writeCharacter(output, o.character);
    }

    public static KillCharacterS2C read(Input input) {
        return new KillCharacterS2C(PacketUtils.readCharacter(input));
    }
}
