package dev.creoii.dungeoneer.network.c2s;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.util.PacketUtils;

public record JoinFactionC2S(Account account, String factionName) {
    public static void write(Output output, JoinFactionC2S o) {
        PacketUtils.writeAccount(output, o.account);
        output.writeString(o.factionName);
    }

    public static JoinFactionC2S read(Input input) {
        return new JoinFactionC2S(PacketUtils.readAccount(input), input.readString());
    }
}
