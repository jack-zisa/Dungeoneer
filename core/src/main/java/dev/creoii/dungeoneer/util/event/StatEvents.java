package dev.creoii.dungeoneer.util.event;

import dev.creoii.dungeoneer.definitions.sided.Entity;
import dev.creoii.dungeoneer.util.stat.Stat;

public class StatEvents {
    public static final Event<Calculate> CALCULATE = Event.create(Calculate.class, events -> (entity, statType, result) -> {
        float modified = result;
        for (Calculate event : events) {
            modified = event.calculate(entity, statType, result);
        }
        return modified;
    });

    @FunctionalInterface
    public interface Calculate {
        float calculate(Entity<?> entity, Stat.Type statType, float result);
    }
}
