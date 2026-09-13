package dev.creoii.dungeoneer.util.stat;

import dev.creoii.dungeoneer.definitions.sided.Entity;
import dev.creoii.dungeoneer.util.event.StatEvents;

public final class StatUtils {
    public static float getCalculatedSpeed(Entity<?> entity, float speed) {
        float result = (4f + 5.6f * (speed / 75f)) * 8f;
        return StatEvents.CALCULATE.invoker().calculate(entity, Stat.Type.SPEED, result);
    }

    public static float getCalculatedDexterity(Entity<?> entity, float dexterity) {
        float result = 1000f / (1.5f + 6.5f * (dexterity / 75f));
        return StatEvents.CALCULATE.invoker().calculate(entity, Stat.Type.DEXTERITY, result);
    }

    public static float getCalculatedVitality(Entity<?> entity, float vitality) {
        float result = 2f + .2407f * vitality;
        return StatEvents.CALCULATE.invoker().calculate(entity, Stat.Type.VITALITY, result);
    }
}
