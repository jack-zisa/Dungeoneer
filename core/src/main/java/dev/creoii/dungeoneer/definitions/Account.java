package dev.creoii.dungeoneer.definitions;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record Account(
    long id,
    String username, String passwordHash,
    int characterSlots,
    List<Long> characters,
    long activeCharacterId,
    long factionId,
    @Nullable LocalDateTime factionJoinDate,
    LocalDateTime lastLoginDate,
    String settings
) {
    public Account(long id, String username, String passwordHash, int characterSlots, LocalDateTime lastLoginDate) {
        this(id, username, passwordHash, characterSlots, createEmptyCharacters(characterSlots), -1L, -1L, null, lastLoginDate, "");
    }

    public Account copyWithFaction(long factionId, @Nullable LocalDateTime factionJoinDate) {
        return new Account(id, username, passwordHash, characterSlots, characters, activeCharacterId, factionId, factionJoinDate, lastLoginDate, settings);
    }

    public static List<Long> createEmptyCharacters(int characterSlots) {
        List<Long> list = new ArrayList<>(characterSlots);
        for (int i = 0; i < characterSlots; ++i)
            list.add(-1L);
        return list;
    }
}
