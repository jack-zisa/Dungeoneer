package dev.creoii.dungeoneer.network.s2c.faction;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Message;
import dev.creoii.dungeoneer.util.PacketUtils;

public record ChatMessageS2C(Message message) {
    public static void write(Output output, ChatMessageS2C o) {
        PacketUtils.writeMessage(output, o.message);
    }

    public static ChatMessageS2C read(Input input) {
        return new ChatMessageS2C(PacketUtils.readMessage(input));
    }
}
