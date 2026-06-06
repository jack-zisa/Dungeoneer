package dev.creoii.dungeoneer.database.definitions;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public record ServerSession(long id, LocalDateTime startTime, @Nullable LocalDateTime endTime) {
}
