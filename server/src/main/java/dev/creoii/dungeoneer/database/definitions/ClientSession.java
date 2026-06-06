package dev.creoii.dungeoneer.database.definitions;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public record ClientSession(long id, long accountId, LocalDateTime startTime, @Nullable LocalDateTime endTime) {
}
