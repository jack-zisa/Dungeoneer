package dev.creoii.dungeoneer.network.s2c;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Character;
import dev.creoii.dungeoneer.util.PacketUtils;
import dev.creoii.dungeoneer.util.Result;
import org.jspecify.annotations.Nullable;

public record CreateCharacterResultS2C(Result result, @Nullable Character character) {
    public static void write(Output output, CreateCharacterResultS2C o) {
        output.writeInt(o.result.ordinal());
        PacketUtils.writeCharacter(output, o.character);
    }

    public static CreateCharacterResultS2C read(Input input) {
        return new CreateCharacterResultS2C(Result.values()[input.readInt()], PacketUtils.readCharacter(input));
    }
}
