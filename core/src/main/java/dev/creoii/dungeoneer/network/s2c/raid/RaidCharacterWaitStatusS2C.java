package dev.creoii.dungeoneer.network.s2c.raid;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.util.PacketUtils;
import org.jspecify.annotations.Nullable;

public record RaidCharacterWaitStatusS2C(@Nullable CharacterDefinition characterDefinition, long accountId) {
    public static void write(Output output, RaidCharacterWaitStatusS2C o) {
        output.writeLong(o.accountId);
        output.writeBoolean(o.characterDefinition != null);
        if (o.characterDefinition != null)
            PacketUtils.writeCharacter(output, o.characterDefinition);
    }

    public static RaidCharacterWaitStatusS2C read(Input input) {
        long accountId = input.readLong();
        if (input.readBoolean())
            return new RaidCharacterWaitStatusS2C(PacketUtils.readCharacter(input), -1L);
        else return new RaidCharacterWaitStatusS2C(null, accountId);
    }
}
