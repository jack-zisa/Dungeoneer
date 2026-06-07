package dev.creoii.dungeoneer.network.s2c.faction;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Faction;
import dev.creoii.dungeoneer.util.PacketUtils;
import dev.creoii.dungeoneer.network.PacketResult;
import org.jspecify.annotations.Nullable;

public record JoinFactionResultS2C(PacketResult result, @Nullable Faction faction) {
    public static void write(Output output, JoinFactionResultS2C o) {
        output.writeInt(o.result.ordinal());
        output.writeBoolean(o.faction != null);
        if (o.faction != null) PacketUtils.writeFaction(output, o.faction);
    }

    public static JoinFactionResultS2C read(Input input) {
        PacketResult result = PacketResult.values()[input.readInt()];

        Faction faction = null;
        if (input.readBoolean()) {
            faction = PacketUtils.readFaction(input);
        }

        return new JoinFactionResultS2C(result, faction);
    }
}
