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
    String settings
) {
    public Account(long id, String username, String passwordHash) {
        this(id, username, passwordHash, new ArrayList<>(), -1L, null, "");
    }

    public Account(long id, String username, String passwordHash, List<Long> characters) {
        this(id, username, passwordHash, characters, -1L, null, "");
    }

    public Account(long id, String username, String passwordHash, List<Long> characters, String settings) {
        this(id, username, passwordHash, characters, -1L, null, settings);
    }
}
