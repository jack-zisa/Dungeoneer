package dev.creoii.dungeoneer.client.game;

public enum AnimationState {
    IDLE_UP,
    IDLE_DOWN,
    IDLE_LEFT,
    IDLE_RIGHT,
    ATTACKING_UP,
    ATTACKING_DOWN,
    ATTACKING_LEFT,
    ATTACKING_RIGHT,
    MOVING_UP,
    MOVING_DOWN,
    MOVING_LEFT,
    MOVING_RIGHT;

    public static AnimationState toIdle(AnimationState state) {
        return switch (state) {
            case ATTACKING_UP, MOVING_UP -> IDLE_UP;
            case ATTACKING_DOWN, MOVING_DOWN -> IDLE_DOWN;
            case ATTACKING_LEFT, MOVING_LEFT -> IDLE_LEFT;
            case ATTACKING_RIGHT, MOVING_RIGHT -> IDLE_RIGHT;
            default -> state;
        };
    }

    public static AnimationState toAttacking(AnimationState state) {
        return switch (state) {
            case IDLE_UP, MOVING_UP -> ATTACKING_UP;
            case IDLE_DOWN, MOVING_DOWN -> ATTACKING_DOWN;
            case IDLE_LEFT, MOVING_LEFT -> ATTACKING_LEFT;
            case IDLE_RIGHT, MOVING_RIGHT -> ATTACKING_RIGHT;
            default -> state;
        };
    }

    public static AnimationState toMoving(AnimationState state) {
        return switch (state) {
            case ATTACKING_UP, IDLE_UP -> MOVING_UP;
            case ATTACKING_DOWN, IDLE_DOWN -> MOVING_DOWN;
            case ATTACKING_LEFT, IDLE_LEFT -> MOVING_LEFT;
            case ATTACKING_RIGHT, IDLE_RIGHT -> MOVING_RIGHT;
            default -> state;
        };
    }
}
