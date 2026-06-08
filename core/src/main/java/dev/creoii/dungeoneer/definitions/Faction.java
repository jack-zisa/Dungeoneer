package dev.creoii.dungeoneer.definitions;

import java.util.LinkedHashMap;
import java.util.List;

public record Faction(long id, String name, String description, List<Account> accounts, LinkedHashMap<Long, Message> recentMessages) {
}
