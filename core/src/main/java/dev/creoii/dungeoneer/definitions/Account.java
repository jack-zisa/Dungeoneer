package dev.creoii.dungeoneer.definitions;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record Account(
    long id,
    String username, String passwordHash,
    List<Long> characters,
    long factionId,
    @Nullable LocalDateTime factionJoinDate,
    LocalDateTime lastLoginDate,
    String settings
) {
    public Account(long id, String username, String passwordHash, LocalDateTime lastLoginDate) {
        this(id, username, passwordHash, new ArrayList<>(), -1L, null, lastLoginDate, "");
    }

    public Account(long id, String username, String passwordHash, LocalDateTime lastLoginDate, List<Long> characters) {
        this(id, username, passwordHash, characters, -1L, null, lastLoginDate, "");
    }

    public Account(long id, String username, String passwordHash, LocalDateTime lastLoginDate, List<Long> characters, String settings) {
        this(id, username, passwordHash, characters, -1L, null, lastLoginDate, settings);
    }

    public Account copyWithFaction(long factionId, @Nullable LocalDateTime factionJoinDate) {
        return new Account(id, username, passwordHash, characters, factionId, factionJoinDate, lastLoginDate, settings);
    }
}
