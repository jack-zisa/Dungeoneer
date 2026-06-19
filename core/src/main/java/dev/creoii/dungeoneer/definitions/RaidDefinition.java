package dev.creoii.dungeoneer.definitions;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public record RaidDefinition(long id, Account attacker, Account target, LocalDateTime startTime, @Nullable LocalDateTime endTime) {
}
