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

    public Account(long id, String username, String passwordHash, String characters) {
        this(id, username, passwordHash, parseCharacterIds(characters), -1L, null, "");
    }

    public Account(long id, String username, String passwordHash, String characters, String settings) {
        this(id, username, passwordHash, parseCharacterIds(characters), -1L, null, settings);
    }

    public Account(long id, String username, String passwordHash, String characters, long factionId, @Nullable LocalDateTime factionJoinDate, String settings) {
        this(id, username, passwordHash, parseCharacterIds(characters), factionId, factionJoinDate, settings);
    }

    private static List<Long> parseCharacterIds(String s) {
        List<Long> list = new ArrayList<>();
        if (s != null && !s.isBlank()) {
            for (String id : s.split("\\|")) {
                try {
                    list.add(Long.parseLong(id));
                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return list;
    }

    public static String compressCharacterIds(List<Long> characterIds) {
        return String.join("|", characterIds.stream().map(String::valueOf).toList());
    }
}
