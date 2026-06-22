package dev.creoii.dungeoneer.client.util;

public enum RenderLayer {
    GROUND(0),
    OBJECT(1),
    OBJECT_OUTLINED(1),
    CEILING(2);

    private final int priority;

    RenderLayer(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
