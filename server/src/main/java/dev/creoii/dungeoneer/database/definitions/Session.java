package dev.creoii.dungeoneer.database.definitions;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public record Session(long id, long accountId, LocalDateTime startTime, @Nullable LocalDateTime endTime) {
}
