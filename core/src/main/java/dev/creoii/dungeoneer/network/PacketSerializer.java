package dev.creoii.dungeoneer.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.network.c2s.account.AuthenticateC2S;
import dev.creoii.dungeoneer.network.c2s.account.LoginC2S;
import dev.creoii.dungeoneer.network.s2c.account.AuthenticateS2C;
import dev.creoii.dungeoneer.network.s2c.account.LoginResultS2C;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class PacketSerializer extends Serializer<Object> {
    private final BiConsumer<Output, Object> FAILED_WRITE_CONSUMER = (_, _) -> {};
    private final Function<Input, ?> FAILED_READ_CONSUMER = (_) -> null;
    private final Map<Class<?>, BiConsumer<Output, Object>> WRITE_SCHEMA = new HashMap<>();
    private final Map<Class<?>, Function<Input, ?>> READ_SCHEMA = new HashMap<>();
    public static final PacketSerializer INSTANCE = new PacketSerializer();

    @SuppressWarnings("unchecked")
    public <T> void register(Class<T> clazz, BiConsumer<Output, T> writer, Function<Input, T> reader) {
        WRITE_SCHEMA.put(clazz, (BiConsumer<Output, Object>) writer);
        READ_SCHEMA.put(clazz, reader);
    }

    @Override
    public void write(Kryo kryo, Output output, Object o) {
        WRITE_SCHEMA.getOrDefault(o.getClass(), FAILED_WRITE_CONSUMER).accept(output, o);
    }

    @Override
    public Object read(Kryo kryo, Input input, Class<?> type) {
        return READ_SCHEMA.getOrDefault(type, FAILED_READ_CONSUMER).apply(input);
    }

    public boolean isValidPacket(Object o) {
        return READ_SCHEMA.containsKey(o.getClass()) && WRITE_SCHEMA.containsKey(o.getClass());
    }

    public static void registerDefault(Kryo kryo) {
        kryo.register(LoginC2S.class, PacketSerializer.INSTANCE);
        kryo.register(AuthenticateC2S.class, PacketSerializer.INSTANCE);
        kryo.register(AuthenticateS2C.class, PacketSerializer.INSTANCE);
        kryo.register(LoginResultS2C.class, PacketSerializer.INSTANCE);

        PacketSerializer.INSTANCE.register(LoginC2S.class, LoginC2S::write, LoginC2S::read);
        PacketSerializer.INSTANCE.register(AuthenticateC2S.class, AuthenticateC2S::write, AuthenticateC2S::read);
        PacketSerializer.INSTANCE.register(AuthenticateS2C.class, AuthenticateS2C::write, AuthenticateS2C::read);
        PacketSerializer.INSTANCE.register(LoginResultS2C.class, LoginResultS2C::write, LoginResultS2C::read);
    }
}
