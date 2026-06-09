package dev.creoii.dungeoneer.definitions;

import java.time.LocalDateTime;

public record DungeonMap(long id, long accountId, byte[] mapData, LocalDateTime lastEditDate) {
}
