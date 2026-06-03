package dev.creoii.dungeoneer.network.s2c.account;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record LoginResultS2C(int resultId) {
    public static final Codec<LoginResultS2C> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.INT.fieldOf("username").forGetter(LoginResultS2C::resultId)
        ).apply(instance, LoginResultS2C::new);
    });

    public LoginResultS2C(Result result) {
        this(result.ordinal());
    }

    public static void write(Output output, LoginResultS2C o) {
        output.writeInt(o.resultId);
    }

    public static LoginResultS2C read(Input input) {
        return new LoginResultS2C(input.readInt());
    }

    public enum Result {
        SUCCESS,
        FAIL
    }
}
