package dev.creoii.dungeoneer.network.s2c.character;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Faction;
import dev.creoii.dungeoneer.util.PacketUtils;
import org.jspecify.annotations.Nullable;

public record SendFactionS2C(@Nullable Faction faction) {
    public static void write(Output output, SendFactionS2C o) {
        output.writeBoolean(o.faction == null);
        if (o.faction == null)
            return;
        PacketUtils.writeFaction(output, o.faction);
    }

    public static SendFactionS2C read(Input input) {
        if (input.readBoolean())
            return new SendFactionS2C(null);
        return new SendFactionS2C(PacketUtils.readFaction(input));
    }
}
