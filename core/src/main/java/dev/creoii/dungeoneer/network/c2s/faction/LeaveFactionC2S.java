package dev.creoii.dungeoneer.network.c2s.faction;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.util.PacketUtils;

public record LeaveFactionC2S(Account account) {
    public static void write(Output output, LeaveFactionC2S o) {
        PacketUtils.writeAccount(output, o.account);
    }

    public static LeaveFactionC2S read(Input input) {
        return new LeaveFactionC2S(PacketUtils.readAccount(input));
    }
}
