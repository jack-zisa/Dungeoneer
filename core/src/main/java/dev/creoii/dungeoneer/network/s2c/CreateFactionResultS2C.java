package dev.creoii.dungeoneer.network.s2c;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Faction;
import dev.creoii.dungeoneer.util.PacketUtils;
import dev.creoii.dungeoneer.network.PacketResult;
import org.jspecify.annotations.Nullable;

public record CreateFactionResultS2C(PacketResult result, @Nullable Faction faction) {
    public static void write(Output output, CreateFactionResultS2C o) {
        output.writeInt(o.result.ordinal());
        output.writeBoolean(o.faction != null);
        if (o.faction != null) PacketUtils.writeFaction(output, o.faction);
    }

    public static CreateFactionResultS2C read(Input input) {
        PacketResult result = PacketResult.values()[input.readInt()];

        Faction faction = null;
        if (input.readBoolean()) {
            faction = PacketUtils.readFaction(input);
        }

        return new CreateFactionResultS2C(result, faction);
    }
}
