package dev.creoii.dungeoneer.util.event;

import dev.creoii.dungeoneer.definitions.sided.Entity;

public final class HealEvents {
    public static final Event<Modify> MODIFY = Event.create(Modify.class, events -> (entity, amount) -> {
        int modified = amount;
        for (Modify event : events) {
            modified = event.modifyHeal(entity, amount);
        }
        return modified;
    });

    @FunctionalInterface
    public interface Modify {
        int modifyHeal(Entity entity, int amount);
    }
}
