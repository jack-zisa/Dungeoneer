package dev.creoii.dungeoneer.util.event;

import dev.creoii.dungeoneer.definitions.sided.Entity;
import dev.creoii.dungeoneer.definitions.sided.Raid;

public final class DamageEvents {
    public static final Event<Modify> MODIFY = Event.create(Modify.class, events -> (entity, damage) -> {
        int modified = damage;
        for (Modify event : events) {
            modified = event.modifyDamage(entity, damage);
        }
        return modified;
    });

    @FunctionalInterface
    public interface Modify {
        int modifyDamage(Entity entity, int damage);
    }
}
