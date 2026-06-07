package dev.creoii.dungeoneer.network.c2s.faction;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record SearchFactionC2S(String search) {
    public static void write(Output output, SearchFactionC2S o) {
        output.writeString(o.search);
    }

    public static SearchFactionC2S read(Input input) {
        return new SearchFactionC2S(input.readString());
    }
}
