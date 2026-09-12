package dev.creoii.dungeoneer.network.s2c.character;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.util.PacketUtils;

import java.util.ArrayList;
import java.util.List;

public record SendCharactersS2C(List<CharacterDefinition> characters) {
    public static void write(Output output, SendCharactersS2C o) {
        int size = o.characters.size();
        output.writeInt(size, true);
        for (int i = 0; i < size; ++i) {
            PacketUtils.writeCharacter(output, o.characters.get(i));
        }
    }

    public static SendCharactersS2C read(Input input) {
        List<CharacterDefinition> characters = new ArrayList<>();
        int size = input.readInt(true);
        for (int i = 0; i < size; ++i) {
            characters.add(PacketUtils.readCharacter(input));
        }
        return new SendCharactersS2C(characters);
    }
}
