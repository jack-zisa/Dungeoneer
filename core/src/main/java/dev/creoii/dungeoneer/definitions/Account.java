package dev.creoii.dungeoneer.definitions;

import java.util.ArrayList;
import java.util.List;

public record Account(long id, String username, String passwordHash, List<Long> characters, String settings) {
    public Account(long id, String username, String passwordHash) {
        this(id, username, passwordHash, new ArrayList<>(), "");
    }

    public Account(long id, String username, String passwordHash, String characters) {
        this(id, username, passwordHash, parseCharacterIds(characters), "");
    }

    public Account(long id, String username, String passwordHash, String characters, String settings) {
        this(id, username, passwordHash, parseCharacterIds(characters), settings);
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
