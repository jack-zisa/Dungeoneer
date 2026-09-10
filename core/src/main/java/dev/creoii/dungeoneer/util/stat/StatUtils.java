package dev.creoii.dungeoneer.util.stat;

public final class StatUtils {
    public static float getCalculatedSpeed(float speed) {
        return (4f + 5.6f * (speed / 75f)) * 8f;
    }

    public static float getCalculatedAttackSpeed(float attackSpeed) {
        return 1000f / (1.5f + 6.5f * (attackSpeed / 75f));
    }
}
