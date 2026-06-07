package dev.creoii.dungeoneer.network.s2c.account;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.util.PacketUtils;
import dev.creoii.dungeoneer.network.PacketResult;
import org.jspecify.annotations.Nullable;

public record LoginResultS2C(PacketResult result, @Nullable Account account) {
    public static void write(Output output, LoginResultS2C o) {
        output.writeInt(o.result.ordinal());
        PacketUtils.writeAccount(output, o.account);
    }

    public static LoginResultS2C read(Input input) {
        return new LoginResultS2C(PacketResult.values()[input.readInt()], PacketUtils.readAccount(input));
    }
}
