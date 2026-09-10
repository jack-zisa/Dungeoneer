package dev.creoii.dungeoneer.definitions;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;

import java.util.LinkedHashMap;

public record Faction(long id, String name, String description, Long2ObjectArrayMap<Account> accounts, LinkedHashMap<Long, Message> recentMessages) {
}
