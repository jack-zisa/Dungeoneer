package dev.creoii.dungeoneer.network.c2s.account;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record LoginC2S(String username, String password) {
    public static final Codec<LoginC2S> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.STRING.fieldOf("username").forGetter(LoginC2S::username),
            Codec.STRING.fieldOf("password").forGetter(LoginC2S::password)
        ).apply(instance, LoginC2S::new);
    });

    public static void write(Output output, LoginC2S o) {
        output.writeString(o.username);
        output.writeString(o.password);
    }

    public static LoginC2S read(Input input) {
        return new LoginC2S(input.readString(), input.readString());
    }
}
