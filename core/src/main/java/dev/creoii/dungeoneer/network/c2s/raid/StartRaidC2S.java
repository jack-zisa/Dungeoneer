package dev.creoii.dungeoneer.network.c2s.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.util.PacketUtils;

public record StartRaidC2S(long raidId, CharacterDefinition character) {
    public static void write(Output output, StartRaidC2S o) {
        output.writeLong(o.raidId);
        PacketUtils.writeCharacter(output, o.character);
    }

    public static StartRaidC2S read(Input input) {
        return new StartRaidC2S(input.readLong(), PacketUtils.readCharacter(input));
    }
}
