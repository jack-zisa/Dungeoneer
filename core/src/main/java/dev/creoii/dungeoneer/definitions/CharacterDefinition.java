package dev.creoii.dungeoneer.definitions;

import dev.creoii.dungeoneer.definitions.item.inventory.Inventory;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public record CharacterDefinition(long id, long accountId, CharacterClass characterClass, Inventory equipment, @Nullable LocalDateTime deathDate) {
    public boolean isDead() {
        return deathDate != null;
    }
}
