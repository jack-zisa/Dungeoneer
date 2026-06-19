package dev.creoii.dungeoneer.network.c2s.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.util.PacketUtils;

public record JoinOrCreateRaidC2S(Account attacker, CharacterDefinition characterDefinition, int requiredCharacters) {
    public static void write(Output output, JoinOrCreateRaidC2S o) {
        PacketUtils.writeAccount(output, o.attacker);
        PacketUtils.writeCharacter(output, o.characterDefinition);
        output.writeInt(o.requiredCharacters);
    }

    public static JoinOrCreateRaidC2S read(Input input) {
        return new JoinOrCreateRaidC2S(PacketUtils.readAccount(input), PacketUtils.readCharacter(input), input.readInt());
    }
}
