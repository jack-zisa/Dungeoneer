package dev.creoii.dungeoneer.util;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.definitions.Character;
import dev.creoii.dungeoneer.definitions.CharacterClass;
import dev.creoii.dungeoneer.definitions.Faction;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PacketUtils {
    @Nullable
    public static CharacterClass readCharacterClass(Input input) {
        return CharacterClass.parse(input.readString());
    }

    public static void writeCharacterClass(Output output, CharacterClass characterClass) {
        output.writeString(characterClass.id().toLowerCase(Locale.ROOT));
    }

    @Nullable
    public static Character readCharacter(Input input) {
        long id = input.readLong();
        long accountId = input.readLong();
        CharacterClass characterClass = readCharacterClass(input);
        if (characterClass == null) return null;
        return new Character(id, accountId, characterClass);
    }

    public static void writeCharacter(Output output, Character character) {
        output.writeLong(character.id());
        output.writeLong(character.accountId());
        writeCharacterClass(output, character.characterClass());
    }

    public static Account readAccount(Input input) {
        long id = input.readLong();
        String username = input.readString();

        List<Long> characterIds = new ArrayList<>();
        int size = input.readInt();
        for (int i = 0; i < size; ++i) {
            characterIds.add(input.readLong());
        }

        long factionId = input.readLong();
        String factionJoinDate = input.readString();
        return new Account(id, username, "", characterIds, factionId, factionJoinDate.isBlank() ? null : LocalDateTime.parse(factionJoinDate), input.readString());
    }

    public static void writeAccount(Output output, @Nullable Account account) {
        if (account != null) {
            output.writeLong(account.id());
            output.writeString(account.username());
            int size = account.characters().size();
            output.writeInt(size);
            for (int i = 0; i < size; ++i) {
                output.writeLong(account.characters().get(i));
            }
            output.writeLong(account.factionId());
            output.writeString(account.factionJoinDate() == null ? "" : account.factionJoinDate().toString());
            output.writeString(account.settings());
        } else {
            output.writeLong(-1L);
            output.writeString("");
            output.writeInt(0);
            output.writeLong(-1L);
            output.writeString("");
            output.writeString("");
        }
    }

    public static Faction readFaction(Input input) {
        long id = input.readLong();
        String name = input.readString();

        List<Long> accountIds = new ArrayList<>();
        long size = input.readInt();
        for (int i = 0; i < size; ++i) {
            accountIds.add(input.readLong());
        }
        return new Faction(id, name, accountIds);
    }

    public static void writeFaction(Output output, Faction faction) {
        output.writeLong(faction.id());
        output.writeString(faction.name());
        int size = faction.accounts().size();
        output.writeInt(size);
        for (int i = 0; i < size; ++i) {
            output.writeLong(faction.accounts().get(i));
        }
    }
}
