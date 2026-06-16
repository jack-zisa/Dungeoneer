package dev.creoii.dungeoneer.util;

public enum Direction {
    UP(new float[]{1f, 0f}),
    DOWN(new float[]{-1f, 0f}),
    LEFT(new float[]{0f, -1f}),
    RIGHT(new float[]{0f, 1f});

    private final float[] vector;

    Direction(float[] vector) {
        this.vector = vector;
    }

    public float[] getVector() {
        return vector;
    }
}
