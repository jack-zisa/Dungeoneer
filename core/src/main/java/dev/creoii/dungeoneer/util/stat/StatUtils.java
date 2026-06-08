package dev.creoii.dungeoneer.util.stat;

public final class StatUtils {
    public static float getCalculatedSpeed(int speed) {
        return (4f + 5.6f * (speed / 75f)) * 8f;
    }
}
