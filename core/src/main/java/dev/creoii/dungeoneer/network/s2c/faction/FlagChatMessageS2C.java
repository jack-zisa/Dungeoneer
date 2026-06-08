package dev.creoii.dungeoneer.network.s2c.faction;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Message;
import dev.creoii.dungeoneer.util.PacketUtils;

public record FlagChatMessageS2C(long localId, Message message) {
    public static void write(Output output, FlagChatMessageS2C o) {
        output.writeLong(o.localId);
        PacketUtils.writeMessage(output, o.message);
    }

    public static FlagChatMessageS2C read(Input input) {
        return new FlagChatMessageS2C(input.readLong(), PacketUtils.readMessage(input));
    }
}
