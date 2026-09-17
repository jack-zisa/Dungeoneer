package dev.creoii.dungeoneer.util;

import dev.creoii.dungeoneer.definitions.sided.Entity;

public interface EntityOwnable {
    Entity<?> getOwner();

    void setOwner(Entity<?> owner);
}
