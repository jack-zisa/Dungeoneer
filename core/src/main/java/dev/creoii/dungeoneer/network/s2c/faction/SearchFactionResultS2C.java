package dev.creoii.dungeoneer.network.s2c.faction;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Faction;
import dev.creoii.dungeoneer.network.PacketResult;
import dev.creoii.dungeoneer.util.PacketUtils;

import java.util.ArrayList;
import java.util.List;

public record SearchFactionResultS2C(PacketResult result, List<Faction> factions) {
    public static void write(Output output, SearchFactionResultS2C o) {
        output.writeInt(o.result.ordinal());
        output.writeInt(o.factions.size(), true);
        for (Faction faction : o.factions) {
            PacketUtils.writeFaction(output, faction);
        }
    }

    public static SearchFactionResultS2C read(Input input) {
        PacketResult result = PacketResult.values()[input.readInt()];
        int size = input.readInt(true);

        List<Faction> factions = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            factions.add(PacketUtils.readFaction(input));
        }

        return new SearchFactionResultS2C(result, factions);
    }
}
