package dev.creoii.dungeoneer.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

public final class NetworkUtils {
    public static List<Long> parseIds(String s) {
        List<Long> list = new ArrayList<>();
        if (s != null && !s.isBlank()) {
            for (String id : s.split(",")) {
                try {
                    Long l = Long.parseLong(id);
                    if (l == -1L || !list.contains(l))
                        list.add(l);
                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return list;
    }

    public static String compressIds(List<Long> ids) {
        return String.join(",", ids.stream().map(String::valueOf).toList());
    }

    public static String compressIds(LongStream ids) {
        return String.join(",", ids.mapToObj(String::valueOf).toList());
    }
}
