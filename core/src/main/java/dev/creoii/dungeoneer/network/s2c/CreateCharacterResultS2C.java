package dev.creoii.dungeoneer.network.s2c;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Character;
import dev.creoii.dungeoneer.util.PacketUtils;
import dev.creoii.dungeoneer.network.PacketResult;
import org.jspecify.annotations.Nullable;

public record CreateCharacterResultS2C(PacketResult result, int index, @Nullable Character character) {
    public static void write(Output output, CreateCharacterResultS2C o) {
        output.writeInt(o.result.ordinal());
        output.writeInt(o.index);
        output.writeBoolean(o.character != null);
        if (o.character != null) PacketUtils.writeCharacter(output, o.character);
    }

    public static CreateCharacterResultS2C read(Input input) {
        PacketResult result = PacketResult.values()[input.readInt()];
        int index = input.readInt();

        Character character = null;
        if (input.readBoolean()) {
            character = PacketUtils.readCharacter(input);
        }

        return new CreateCharacterResultS2C(result, index, character);
    }
}
