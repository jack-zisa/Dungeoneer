package dev.creoii.dungeoneer.definitions;

import java.util.List;

public record Faction(long id, String name, List<Long> accounts) {
}
