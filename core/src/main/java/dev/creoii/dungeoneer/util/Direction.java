package dev.creoii.dungeoneer.util;

public enum Direction {
    UP(new float[]{0f, 1f}),
    DOWN(new float[]{0f, -1f}),
    LEFT(new float[]{-1f, 0f}),
    RIGHT(new float[]{1f, 0f});

    private final float[] vector;

    Direction(float[] vector) {
        this.vector = vector;
    }

    public float[] getVector() {
        return vector;
    }
}
