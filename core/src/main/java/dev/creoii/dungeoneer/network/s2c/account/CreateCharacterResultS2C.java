package dev.creoii.dungeoneer.network.s2c.account;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Character;
import dev.creoii.dungeoneer.util.PacketUtils;
import org.jspecify.annotations.Nullable;

public record CreateCharacterResultS2C(int resultId, @Nullable Character character) {
    public CreateCharacterResultS2C(LoginResultS2C.Result result, @Nullable Character character) {
        this(result.ordinal(), character);
    }

    public static void write(Output output, CreateCharacterResultS2C o) {
        output.writeInt(o.resultId);
        PacketUtils.writeCharacter(output, o.character);
    }

    public static CreateCharacterResultS2C read(Input input) {
        return new CreateCharacterResultS2C(input.readInt(), PacketUtils.readCharacter(input));
    }

    public enum Result {
        SUCCESS,
        FAIL
    }
}
