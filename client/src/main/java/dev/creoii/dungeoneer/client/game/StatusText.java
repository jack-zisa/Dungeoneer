package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.graphics.Color;

public record StatusText(String text, Color color, long duration, long startTime) implements Comparable<StatusText> {
    public long getAge() {
        if (duration <= 0) return 0L;
        return System.currentTimeMillis() - startTime;
    }

    public boolean isExpired() {
        if (duration <= 0) return false;
        return getAge() >= duration;
    }

    @Override
    public int compareTo(StatusText o) {
        if (o.getAge() == getAge()) return 0;
        return o.getAge() > getAge() ? -1 : 1;
    }
}
