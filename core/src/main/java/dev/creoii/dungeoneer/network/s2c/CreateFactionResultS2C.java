package dev.creoii.dungeoneer.network.s2c;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Faction;
import dev.creoii.dungeoneer.util.PacketUtils;
import dev.creoii.dungeoneer.util.Result;
import org.jspecify.annotations.Nullable;

public record CreateFactionResultS2C(Result result, @Nullable Faction faction) {
    public static void write(Output output, CreateFactionResultS2C o) {
        output.writeInt(o.result.ordinal());
        PacketUtils.writeFaction(output, o.faction);
    }

    public static CreateFactionResultS2C read(Input input) {
        return new CreateFactionResultS2C(Result.values()[input.readInt()], PacketUtils.readFaction(input));
    }
}
