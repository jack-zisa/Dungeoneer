package dev.creoii.dungeoneer.definitions;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;

public record RaidDefinition(long id, List<Account> attackers, Account target, int requiredCharacters, LocalDateTime startTime, @Nullable LocalDateTime endTime) {
}
