package dev.creoii.dungeoneer.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.network.c2s.account.RequestLoginC2S;
import dev.creoii.dungeoneer.network.c2s.account.LoginC2S;
import dev.creoii.dungeoneer.network.c2s.dungeon.RequestDungeonMapC2S;
import dev.creoii.dungeoneer.network.c2s.dungeon.SaveDungeonMapC2S;
import dev.creoii.dungeoneer.network.c2s.character.*;
import dev.creoii.dungeoneer.network.c2s.faction.*;
import dev.creoii.dungeoneer.network.c2s.raid.AttackC2S;
import dev.creoii.dungeoneer.network.c2s.raid.EndRaidC2S;
import dev.creoii.dungeoneer.network.c2s.raid.RequestRaidTargetC2S;
import dev.creoii.dungeoneer.network.c2s.raid.StartRaidC2S;
import dev.creoii.dungeoneer.network.s2c.LoadDataS2C;
import dev.creoii.dungeoneer.network.s2c.SyncDataS2C;
import dev.creoii.dungeoneer.network.s2c.account.AuthenticateS2C;
import dev.creoii.dungeoneer.network.s2c.account.LoginResultS2C;
import dev.creoii.dungeoneer.network.s2c.character.CharacterMoveS2C;
import dev.creoii.dungeoneer.network.s2c.character.CreateCharacterResultS2C;
import dev.creoii.dungeoneer.network.s2c.character.SendCharactersS2C;
import dev.creoii.dungeoneer.network.s2c.character.SendFactionS2C;
import dev.creoii.dungeoneer.network.s2c.dungeon.SendDungeonMapS2C;
import dev.creoii.dungeoneer.network.s2c.faction.*;
import dev.creoii.dungeoneer.network.s2c.raid.AttackResultS2C;
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
        kryo.register(CreateFactionC2S.class, PacketSerializer.INSTANCE);
        kryo.register(JoinFactionC2S.class, PacketSerializer.INSTANCE);
        kryo.register(LeaveFactionC2S.class, PacketSerializer.INSTANCE);
        kryo.register(RequestRaidTargetC2S.class, PacketSerializer.INSTANCE);
        kryo.register(StartRaidC2S.class, PacketSerializer.INSTANCE);
        kryo.register(EndRaidC2S.class, PacketSerializer.INSTANCE);
        kryo.register(DeleteCharacterC2S.class, PacketSerializer.INSTANCE);
        kryo.register(SearchFactionC2S.class, PacketSerializer.INSTANCE);
        kryo.register(RequestFactionC2S.class, PacketSerializer.INSTANCE);
        kryo.register(SelectActiveCharacterC2S.class, PacketSerializer.INSTANCE);
        kryo.register(CharacterMoveC2S.class, PacketSerializer.INSTANCE);
        kryo.register(ChatMessageC2S.class, PacketSerializer.INSTANCE);
        kryo.register(SaveDungeonMapC2S.class, PacketSerializer.INSTANCE);
        kryo.register(RequestDungeonMapC2S.class, PacketSerializer.INSTANCE);
        kryo.register(AttackC2S.class, PacketSerializer.INSTANCE);

        kryo.register(AuthenticateS2C.class, PacketSerializer.INSTANCE);
        kryo.register(LoginResultS2C.class, PacketSerializer.INSTANCE);
        kryo.register(SendCharactersS2C.class, PacketSerializer.INSTANCE);
        kryo.register(CreateCharacterResultS2C.class, PacketSerializer.INSTANCE);
        kryo.register(CreateFactionResultS2C.class, PacketSerializer.INSTANCE);
        kryo.register(JoinFactionResultS2C.class, PacketSerializer.INSTANCE);
        kryo.register(LeaveFactionResultS2C.class, PacketSerializer.INSTANCE);
        kryo.register(SendRaidS2C.class, PacketSerializer.INSTANCE);
        kryo.register(SearchFactionResultS2C.class, PacketSerializer.INSTANCE);
        kryo.register(SendFactionS2C.class, PacketSerializer.INSTANCE);
        kryo.register(CharacterMoveS2C.class, PacketSerializer.INSTANCE);
        kryo.register(FlagChatMessageS2C.class, PacketSerializer.INSTANCE);
        kryo.register(ChatMessageS2C.class, PacketSerializer.INSTANCE);
        kryo.register(SendDungeonMapS2C.class, PacketSerializer.INSTANCE);
        kryo.register(SyncDataS2C.class, PacketSerializer.INSTANCE);
        kryo.register(LoadDataS2C.class, PacketSerializer.INSTANCE);
        kryo.register(AttackResultS2C.class, PacketSerializer.INSTANCE);

        PacketSerializer.INSTANCE.register(LoginC2S.class, LoginC2S::write, LoginC2S::read);
        PacketSerializer.INSTANCE.register(RequestLoginC2S.class, RequestLoginC2S::write, RequestLoginC2S::read);
        PacketSerializer.INSTANCE.register(RequestCharactersC2S.class, RequestCharactersC2S::write, RequestCharactersC2S::read);
        PacketSerializer.INSTANCE.register(CreateCharacterC2S.class, CreateCharacterC2S::write, CreateCharacterC2S::read);
        PacketSerializer.INSTANCE.register(CreateFactionC2S.class, CreateFactionC2S::write, CreateFactionC2S::read);
        PacketSerializer.INSTANCE.register(JoinFactionC2S.class, JoinFactionC2S::write, JoinFactionC2S::read);
        PacketSerializer.INSTANCE.register(LeaveFactionC2S.class, LeaveFactionC2S::write, LeaveFactionC2S::read);
        PacketSerializer.INSTANCE.register(RequestRaidTargetC2S.class, RequestRaidTargetC2S::write, RequestRaidTargetC2S::read);
        PacketSerializer.INSTANCE.register(StartRaidC2S.class, StartRaidC2S::write, StartRaidC2S::read);
        PacketSerializer.INSTANCE.register(EndRaidC2S.class, EndRaidC2S::write, EndRaidC2S::read);
        PacketSerializer.INSTANCE.register(DeleteCharacterC2S.class, DeleteCharacterC2S::write, DeleteCharacterC2S::read);
        PacketSerializer.INSTANCE.register(SearchFactionC2S.class, SearchFactionC2S::write, SearchFactionC2S::read);
        PacketSerializer.INSTANCE.register(RequestFactionC2S.class, RequestFactionC2S::write, RequestFactionC2S::read);
        PacketSerializer.INSTANCE.register(SelectActiveCharacterC2S.class, SelectActiveCharacterC2S::write, SelectActiveCharacterC2S::read);
        PacketSerializer.INSTANCE.register(CharacterMoveC2S.class, CharacterMoveC2S::write, CharacterMoveC2S::read);
        PacketSerializer.INSTANCE.register(ChatMessageC2S.class, ChatMessageC2S::write, ChatMessageC2S::read);
        PacketSerializer.INSTANCE.register(SaveDungeonMapC2S.class, SaveDungeonMapC2S::write, SaveDungeonMapC2S::read);
        PacketSerializer.INSTANCE.register(RequestDungeonMapC2S.class, RequestDungeonMapC2S::write, RequestDungeonMapC2S::read);
        PacketSerializer.INSTANCE.register(AttackC2S.class, AttackC2S::write, AttackC2S::read);

        PacketSerializer.INSTANCE.register(AuthenticateS2C.class, AuthenticateS2C::write, AuthenticateS2C::read);
        PacketSerializer.INSTANCE.register(LoginResultS2C.class, LoginResultS2C::write, LoginResultS2C::read);
        PacketSerializer.INSTANCE.register(SendCharactersS2C.class, SendCharactersS2C::write, SendCharactersS2C::read);
        PacketSerializer.INSTANCE.register(CreateCharacterResultS2C.class, CreateCharacterResultS2C::write, CreateCharacterResultS2C::read);
        PacketSerializer.INSTANCE.register(CreateFactionResultS2C.class, CreateFactionResultS2C::write, CreateFactionResultS2C::read);
        PacketSerializer.INSTANCE.register(JoinFactionResultS2C.class, JoinFactionResultS2C::write, JoinFactionResultS2C::read);
        PacketSerializer.INSTANCE.register(LeaveFactionResultS2C.class, LeaveFactionResultS2C::write, LeaveFactionResultS2C::read);
        PacketSerializer.INSTANCE.register(SendRaidS2C.class, SendRaidS2C::write, SendRaidS2C::read);
        PacketSerializer.INSTANCE.register(SearchFactionResultS2C.class, SearchFactionResultS2C::write, SearchFactionResultS2C::read);
        PacketSerializer.INSTANCE.register(SendFactionS2C.class, SendFactionS2C::write, SendFactionS2C::read);
        PacketSerializer.INSTANCE.register(CharacterMoveS2C.class, CharacterMoveS2C::write, CharacterMoveS2C::read);
        PacketSerializer.INSTANCE.register(FlagChatMessageS2C.class, FlagChatMessageS2C::write, FlagChatMessageS2C::read);
        PacketSerializer.INSTANCE.register(ChatMessageS2C.class, ChatMessageS2C::write, ChatMessageS2C::read);
        PacketSerializer.INSTANCE.register(SendDungeonMapS2C.class, SendDungeonMapS2C::write, SendDungeonMapS2C::read);
        PacketSerializer.INSTANCE.register(SyncDataS2C.class, SyncDataS2C::write, SyncDataS2C::read);
        PacketSerializer.INSTANCE.register(LoadDataS2C.class, LoadDataS2C::write, LoadDataS2C::read);
        PacketSerializer.INSTANCE.register(AttackResultS2C.class, AttackResultS2C::write, AttackResultS2C::read);
    }
}
