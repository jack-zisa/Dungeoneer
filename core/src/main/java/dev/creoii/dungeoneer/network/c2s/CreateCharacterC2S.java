package dev.creoii.dungeoneer.network.c2s;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.definitions.CharacterClass;
import dev.creoii.dungeoneer.util.PacketUtils;

public record CreateCharacterC2S(long accountId, int index, CharacterClass characterClass) {
    public static void write(Output output, CreateCharacterC2S o) {
        output.writeLong(o.accountId);
        output.writeInt(o.index);
        PacketUtils.writeCharacterClass(output, o.characterClass);
    }

    public static CreateCharacterC2S read(Input input) {
        return new CreateCharacterC2S(input.readLong(), input.readInt(), PacketUtils.readCharacterClass(input));
    }
}
