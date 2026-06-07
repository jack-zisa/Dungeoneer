package dev.creoii.dungeoneer.util;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.*;
import dev.creoii.dungeoneer.definitions.Character;
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
        if (id == -1L) return null;
        long accountId = input.readLong();
        CharacterClass characterClass = readCharacterClass(input);
        if (characterClass == null) return null;
        return new Character(id, accountId, characterClass);
    }

    public static void writeCharacter(Output output, @Nullable Character character) {
        if (character == null) {
            output.writeLong(-1L);
            return;
        }
        output.writeLong(character.id());
        output.writeLong(character.accountId());
        writeCharacterClass(output, character.characterClass());
    }

    public static Account readAccount(Input input) {
        long id = input.readLong();
        String username = input.readString();
        int characterSlots = input.readInt();

        int size = input.readInt();
        List<Long> characterIds = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            characterIds.add(input.readLong());
        }

        long activeCharacterId = input.readLong();
        long factionId = input.readLong();
        String factionJoinDate = input.readString();
        String lastLoginDate = input.readString();
        return new Account(id, username, "", characterSlots, characterIds, activeCharacterId, factionId,
            factionJoinDate.isBlank() ? null : LocalDateTime.parse(factionJoinDate),
            lastLoginDate.isBlank() ? null : LocalDateTime.parse(lastLoginDate),
            input.readString());
    }

    public static void writeAccount(Output output, @Nullable Account account) {
        if (account != null) {
            output.writeLong(account.id());
            output.writeString(account.username());
            output.writeInt(account.characterSlots());
            output.writeInt(account.characters().size());
            for (Long id : account.characters()) {
                output.writeLong(id);
            }
            output.writeLong(account.activeCharacterId());
            output.writeLong(account.factionId());
            output.writeString(account.factionJoinDate() == null ? "" : account.factionJoinDate().toString());
            output.writeString(account.lastLoginDate() == null ? "" : account.lastLoginDate().toString());
            output.writeString(account.settings());
        } else {
            output.writeLong(-1L); // account id
            output.writeString(""); // username
            output.writeInt(0); // character slots
            output.writeInt(0); // characters size
            output.writeLong(-1L); // active character id
            output.writeLong(-1L); // faction id
            output.writeString(""); // faction join date
            output.writeString(""); // last login date
            output.writeString(""); // settings
        }
    }

    public static Faction readFaction(Input input) {
        long id = input.readLong();
        String name = input.readString();
        String description = input.readString();

        List<Account> accountIds = new ArrayList<>();
        long size = input.readInt();
        for (int i = 0; i < size; ++i) {
            accountIds.add(readAccount(input));
        }
        return new Faction(id, name, description, accountIds);
    }

    public static void writeFaction(Output output, Faction faction) {
        output.writeLong(faction.id());
        output.writeString(faction.name());
        output.writeString(faction.description());
        int size = faction.accounts().size();
        output.writeInt(size);
        for (int i = 0; i < size; ++i) {
            writeAccount(output, faction.accounts().get(i));
        }
    }

    public static Raid readRaid(Input input) {
        long id = input.readLong();
        Account attacker = readAccount(input);
        Account target = readAccount(input);
        String startTime = input.readString();
        String endTime = input.readString();
        return new Raid(id, attacker, target, startTime.isBlank() ? null : LocalDateTime.parse(startTime), endTime.isBlank() ? null : LocalDateTime.parse(endTime));
    }

    public static void writeRaid(Output output, Raid raid) {
        output.writeLong(raid.id());
        writeAccount(output, raid.attacker());
        writeAccount(output, raid.target());
        output.writeString(raid.startTime().toString());
        output.writeString(raid.endTime() == null ? "" : raid.endTime().toString());
    }
}
