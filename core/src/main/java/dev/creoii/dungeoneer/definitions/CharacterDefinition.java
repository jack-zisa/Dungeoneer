package dev.creoii.dungeoneer.definitions;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public record CharacterDefinition(long id, long accountId, CharacterClass characterClass, @Nullable LocalDateTime deathDate) {
    public boolean isDead() {
        return deathDate != null;
    }
}
