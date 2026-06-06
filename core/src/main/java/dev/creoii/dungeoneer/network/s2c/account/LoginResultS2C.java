package dev.creoii.dungeoneer.network.s2c.account;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.util.PacketUtils;
import org.jspecify.annotations.Nullable;

public record LoginResultS2C(int resultId, @Nullable Account account) {
    public LoginResultS2C(Result result, @Nullable Account account) {
        this(result.ordinal(), account);
    }

    public static void write(Output output, LoginResultS2C o) {
        output.writeInt(o.resultId);
        PacketUtils.writeAccount(output, o.account);
    }

    public static LoginResultS2C read(Input input) {
        return new LoginResultS2C(input.readInt(), PacketUtils.readAccount(input));
    }

    public enum Result {
        SUCCESS,
        FAIL
    }
}
