package dev.creoii.dungeoneer.util;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.*;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.util.stat.Stat;
import dev.creoii.dungeoneer.util.stat.StatContainer;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.*;

public final class PacketUtils {
    @Nullable
    public static CharacterClass readCharacterClass(Input input) {
        return DataManager.getCharacterClass(input.readString());
    }

    public static void writeCharacterClass(Output output, CharacterClass characterClass) {
        output.writeString(characterClass.id().toLowerCase(Locale.ROOT));
    }

    @Nullable
    public static CharacterDefinition readCharacter(Input input) {
        long id = input.readLong();
        if (id == -1L) return null;
        long accountId = input.readLong();
        CharacterClass characterClass = readCharacterClass(input);
        if (characterClass == null) return null;
        return new CharacterDefinition(id, accountId, characterClass);
    }

    public static void writeCharacter(Output output, @Nullable CharacterDefinition character) {
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
        int gold = input.readInt();
        int gems = input.readInt();
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
        return new Account(id, username, "", gold, gems, characterSlots, characterIds, activeCharacterId, factionId,
            factionJoinDate.isBlank() ? null : LocalDateTime.parse(factionJoinDate),
            lastLoginDate.isBlank() ? null : LocalDateTime.parse(lastLoginDate));
    }

    public static void writeAccount(Output output, @Nullable Account account) {
        if (account != null) {
            output.writeLong(account.id());
            output.writeString(account.username());
            output.writeInt(account.gold());
            output.writeInt(account.gems());
            output.writeInt(account.characterSlots());
            output.writeInt(account.characters().size());
            for (Long id : account.characters()) {
                output.writeLong(id);
            }
            output.writeLong(account.activeCharacterId());
            output.writeLong(account.factionId());
            output.writeString(account.factionJoinDate() == null ? "" : account.factionJoinDate().toString());
            output.writeString(account.lastLoginDate() == null ? "" : account.lastLoginDate().toString());
        } else {
            output.writeLong(-1L); // account id
            output.writeString(""); // username
            output.writeInt(0); // gold
            output.writeInt(0); // gems
            output.writeInt(0); // character slots
            output.writeInt(0); // characters size
            output.writeLong(-1L); // active character id
            output.writeLong(-1L); // faction id
            output.writeString(""); // faction join date
            output.writeString(""); // last login date
        }
    }

    public static DungeonMap readDungeonMap(Input input) {
        long id = input.readLong();
        long accountId = input.readLong();
        int len = input.readInt();
        byte[] dungeonMap = input.readBytes(len);
        String lastEditDate = input.readString();
        return new DungeonMap(id, accountId, dungeonMap, lastEditDate.isBlank() ? null : LocalDateTime.parse(lastEditDate));
    }

    public static void writeDungeonMap(Output output, DungeonMap dungeonMap) {
        output.writeLong(dungeonMap.id());
        output.writeLong(dungeonMap.accountId());
        output.writeInt(dungeonMap.mapData() == null ? 0 : dungeonMap.mapData().length);
        output.writeBytes(dungeonMap.mapData() == null ? new byte[]{} : dungeonMap.mapData());
        output.writeString(dungeonMap.lastEditDate() == null ? "" : dungeonMap.lastEditDate().toString());
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
        return new Faction(id, name, description, accountIds, new LinkedHashMap<>());
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

    public static RaidDefinition readRaid(Input input) {
        long id = input.readLong();

        List<Account> attackers = new ArrayList<>();
        long size = input.readInt();
        for (int i = 0; i < size; ++i) {
            attackers.add(readAccount(input));
        }

        Account target = readAccount(input);
        int requiredCharacters = input.readInt();
        String startTime = input.readString();
        String endTime = input.readString();
        return new RaidDefinition(id, attackers, target, requiredCharacters, startTime.isBlank() ? null : LocalDateTime.parse(startTime), endTime.isBlank() ? null : LocalDateTime.parse(endTime));
    }

    public static void writeRaid(Output output, RaidDefinition raid) {
        output.writeLong(raid.id());
        output.writeInt(raid.attackers().size());
        for (Account account : raid.attackers()) {
            writeAccount(output, account);
        }
        writeAccount(output, raid.target());
        output.writeInt(raid.requiredCharacters());
        output.writeString(raid.startTime().toString());
        output.writeString(raid.endTime() == null ? "" : raid.endTime().toString());
    }

    public static Stat readStat(Input input) {
        return new Stat(Stat.Type.values()[input.readInt()], input.readInt());
    }

    public static void writeStat(Output output, Stat stat) {
        output.writeInt(stat.type().ordinal());
        output.writeInt(stat.value());
    }

    public static void writeStatContainer(Output output, StatContainer container) {
        writeStat(output, container.health());
        writeStat(output, container.speed());
        writeStat(output, container.attackSpeed());
    }

    public static StatContainer readStatContainer(Input input) {
        return new StatContainer(readStat(input), readStat(input), readStat(input));
    }

    public static Message readMessage(Input input) {
        return new Message(input.readLong(), input.readLong(), input.readLong(), input.readString(), input.readBoolean());
    }

    public static void writeMessage(Output output, Message message) {
        output.writeLong(message.messageId());
        output.writeLong(message.factionId());
        output.writeLong(message.accountId());
        output.writeString(message.text());
        output.writeBoolean(message.flagged());
    }
}
