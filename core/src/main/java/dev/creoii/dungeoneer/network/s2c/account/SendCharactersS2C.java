package dev.creoii.dungeoneer.network.s2c.account;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Character;
import dev.creoii.dungeoneer.util.PacketUtils;

import java.util.ArrayList;
import java.util.List;

public record SendCharactersS2C(List<Character> characters) {
    public static void write(Output output, SendCharactersS2C o) {
        int size = o.characters.size();
        output.writeInt(size);
        for (int i = 0; i < size; ++i) {
            PacketUtils.writeCharacter(output, o.characters.get(i));
        }
    }

    public static SendCharactersS2C read(Input input) {
        List<Character> characters = new ArrayList<>();
        int size = input.readInt();
        for (int i = 0; i < size; ++i) {
            characters.add(PacketUtils.readCharacter(input));
        }
        return new SendCharactersS2C(characters);
    }
}
