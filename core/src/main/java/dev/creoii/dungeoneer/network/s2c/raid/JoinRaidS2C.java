package dev.creoii.dungeoneer.network.s2c.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.util.PacketUtils;

public record JoinRaidS2C(CharacterDefinition characterDefinition) {
    public static void write(Output output, JoinRaidS2C o) {
        PacketUtils.writeCharacter(output, o.characterDefinition);
    }

    public static JoinRaidS2C read(Input input) {
        return new JoinRaidS2C(PacketUtils.readCharacter(input));
    }
}
