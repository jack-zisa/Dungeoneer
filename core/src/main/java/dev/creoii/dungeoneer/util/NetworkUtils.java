package dev.creoii.dungeoneer.util;

import java.util.ArrayList;
import java.util.List;

public final class NetworkUtils {
    public static List<Long> parseIds(String s) {
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

    public static String compressIds(List<Long> characterIds) {
        return String.join("|", characterIds.stream().map(String::valueOf).toList());
    }
}
