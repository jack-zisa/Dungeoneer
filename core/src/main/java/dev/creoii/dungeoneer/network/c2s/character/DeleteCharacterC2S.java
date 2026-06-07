package dev.creoii.dungeoneer.network.c2s.character;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record DeleteCharacterC2S(long accountId, int index) {
    public static void write(Output output, DeleteCharacterC2S o) {
        output.writeLong(o.accountId);
        output.writeInt(o.index);
    }

    public static DeleteCharacterC2S read(Input input) {
        return new DeleteCharacterC2S(input.readLong(), input.readInt());
    }
}
