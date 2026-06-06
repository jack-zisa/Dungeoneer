package dev.creoii.dungeoneer.network.s2c;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Faction;
import dev.creoii.dungeoneer.util.PacketUtils;
import dev.creoii.dungeoneer.network.PacketResult;
import org.jspecify.annotations.Nullable;

public record JoinFactionResultS2C(PacketResult result, @Nullable Faction faction) {
    public static void write(Output output, JoinFactionResultS2C o) {
        output.writeInt(o.result.ordinal());
        PacketUtils.writeFaction(output, o.faction);
    }

    public static JoinFactionResultS2C read(Input input) {
        return new JoinFactionResultS2C(PacketResult.values()[input.readInt()], PacketUtils.readFaction(input));
    }
}
