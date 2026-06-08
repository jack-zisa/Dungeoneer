package dev.creoii.dungeoneer.network.c2s.faction;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Message;
import dev.creoii.dungeoneer.util.PacketUtils;

public record ChatMessageC2S(long localId, Message message) {
    public static void write(Output output, ChatMessageC2S o) {
        output.writeLong(o.localId);
        PacketUtils.writeMessage(output, o.message);
    }

    public static ChatMessageC2S read(Input input) {
        return new ChatMessageC2S(input.readLong(), PacketUtils.readMessage(input));
    }
}
