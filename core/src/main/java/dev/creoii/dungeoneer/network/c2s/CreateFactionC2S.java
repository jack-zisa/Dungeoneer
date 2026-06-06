package dev.creoii.dungeoneer.network.c2s;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.util.PacketUtils;

public record CreateFactionC2S(Account account, String factionName) {
    public static void write(Output output, CreateFactionC2S o) {
        PacketUtils.writeAccount(output, o.account);
        output.writeString(o.factionName);
    }

    public static CreateFactionC2S read(Input input) {
        return new CreateFactionC2S(PacketUtils.readAccount(input), input.readString());
    }
}
