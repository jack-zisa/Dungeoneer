package dev.creoii.dungeoneer.network.c2s;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record ApplySettingsC2S(long accountId, String settings) {
    public static void write(Output output, ApplySettingsC2S o) {
        output.writeLong(o.accountId);
        output.writeString(o.settings);
    }

    public static ApplySettingsC2S read(Input input) {
        return new ApplySettingsC2S(input.readLong(), input.readString());
    }
}
