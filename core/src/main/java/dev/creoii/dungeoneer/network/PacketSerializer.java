package dev.creoii.dungeoneer.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.network.c2s.*;
import dev.creoii.dungeoneer.network.c2s.account.RequestLoginC2S;
import dev.creoii.dungeoneer.network.c2s.account.LoginC2S;
import dev.creoii.dungeoneer.network.c2s.faction.CreateFactionC2S;
import dev.creoii.dungeoneer.network.c2s.faction.JoinFactionC2S;
import dev.creoii.dungeoneer.network.c2s.faction.LeaveFactionC2S;
import dev.creoii.dungeoneer.network.c2s.raid.EndRaidC2S;
import dev.creoii.dungeoneer.network.c2s.raid.RequestRaidTargetC2S;
import dev.creoii.dungeoneer.network.s2c.*;
import dev.creoii.dungeoneer.network.s2c.account.AuthenticateS2C;
import dev.creoii.dungeoneer.network.s2c.account.LoginResultS2C;
import dev.creoii.dungeoneer.network.s2c.faction.CreateFactionResultS2C;
import dev.creoii.dungeoneer.network.s2c.faction.JoinFactionResultS2C;
import dev.creoii.dungeoneer.network.s2c.faction.LeaveFactionResultS2C;
import dev.creoii.dungeoneer.network.s2c.raid.SendRaidS2C;

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
        kryo.register(RequestLoginC2S.class, PacketSerializer.INSTANCE);
        kryo.register(RequestCharactersC2S.class, PacketSerializer.INSTANCE);
        kryo.register(CreateCharacterC2S.class, PacketSerializer.INSTANCE);
        kryo.register(ApplySettingsC2S.class, PacketSerializer.INSTANCE);
        kryo.register(CreateFactionC2S.class, PacketSerializer.INSTANCE);
        kryo.register(JoinFactionC2S.class, PacketSerializer.INSTANCE);
        kryo.register(LeaveFactionC2S.class, PacketSerializer.INSTANCE);
        kryo.register(RequestRaidTargetC2S.class, PacketSerializer.INSTANCE);
        kryo.register(EndRaidC2S.class, PacketSerializer.INSTANCE);

        kryo.register(AuthenticateS2C.class, PacketSerializer.INSTANCE);
        kryo.register(LoginResultS2C.class, PacketSerializer.INSTANCE);
        kryo.register(SendCharactersS2C.class, PacketSerializer.INSTANCE);
        kryo.register(CreateCharacterResultS2C.class, PacketSerializer.INSTANCE);
        kryo.register(CreateFactionResultS2C.class, PacketSerializer.INSTANCE);
        kryo.register(JoinFactionResultS2C.class, PacketSerializer.INSTANCE);
        kryo.register(LeaveFactionResultS2C.class, PacketSerializer.INSTANCE);
        kryo.register(SendRaidS2C.class, PacketSerializer.INSTANCE);

        PacketSerializer.INSTANCE.register(LoginC2S.class, LoginC2S::write, LoginC2S::read);
        PacketSerializer.INSTANCE.register(RequestLoginC2S.class, RequestLoginC2S::write, RequestLoginC2S::read);
        PacketSerializer.INSTANCE.register(RequestCharactersC2S.class, RequestCharactersC2S::write, RequestCharactersC2S::read);
        PacketSerializer.INSTANCE.register(CreateCharacterC2S.class, CreateCharacterC2S::write, CreateCharacterC2S::read);
        PacketSerializer.INSTANCE.register(ApplySettingsC2S.class, ApplySettingsC2S::write, ApplySettingsC2S::read);
        PacketSerializer.INSTANCE.register(CreateFactionC2S.class, CreateFactionC2S::write, CreateFactionC2S::read);
        PacketSerializer.INSTANCE.register(JoinFactionC2S.class, JoinFactionC2S::write, JoinFactionC2S::read);
        PacketSerializer.INSTANCE.register(LeaveFactionC2S.class, LeaveFactionC2S::write, LeaveFactionC2S::read);
        PacketSerializer.INSTANCE.register(RequestRaidTargetC2S.class, RequestRaidTargetC2S::write, RequestRaidTargetC2S::read);
        PacketSerializer.INSTANCE.register(EndRaidC2S.class, EndRaidC2S::write, EndRaidC2S::read);

        PacketSerializer.INSTANCE.register(AuthenticateS2C.class, AuthenticateS2C::write, AuthenticateS2C::read);
        PacketSerializer.INSTANCE.register(LoginResultS2C.class, LoginResultS2C::write, LoginResultS2C::read);
        PacketSerializer.INSTANCE.register(SendCharactersS2C.class, SendCharactersS2C::write, SendCharactersS2C::read);
        PacketSerializer.INSTANCE.register(CreateCharacterResultS2C.class, CreateCharacterResultS2C::write, CreateCharacterResultS2C::read);
        PacketSerializer.INSTANCE.register(CreateFactionResultS2C.class, CreateFactionResultS2C::write, CreateFactionResultS2C::read);
        PacketSerializer.INSTANCE.register(JoinFactionResultS2C.class, JoinFactionResultS2C::write, JoinFactionResultS2C::read);
        PacketSerializer.INSTANCE.register(LeaveFactionResultS2C.class, LeaveFactionResultS2C::write, LeaveFactionResultS2C::read);
        PacketSerializer.INSTANCE.register(SendRaidS2C.class, SendRaidS2C::write, SendRaidS2C::read);
    }
}
