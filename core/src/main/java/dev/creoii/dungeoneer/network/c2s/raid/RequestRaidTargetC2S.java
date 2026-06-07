package dev.creoii.dungeoneer.network.c2s.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.util.PacketUtils;

public record RequestRaidTargetC2S(Account attacker) {
    public static void write(Output output, RequestRaidTargetC2S o) {
        PacketUtils.writeAccount(output, o.attacker);
    }

    public static RequestRaidTargetC2S read(Input input) {
        return new RequestRaidTargetC2S(PacketUtils.readAccount(input));
    }
}
