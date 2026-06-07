package dev.creoii.dungeoneer.server.game;

import com.badlogic.gdx.math.Vector2;
import dev.creoii.dungeoneer.definitions.Character;
import dev.creoii.dungeoneer.definitions.sided.SidedCharacter;

public class ServerCharacter implements SidedCharacter {
    public static final int LEFT  = 1;
    public static final int RIGHT = 2;
    public static final int UP    = 4;
    public static final int DOWN  = 8;

    private final Character character;
    private final Vector2 pos;
    private final Vector2 velocity;
    private float speed;
    private int movementFlags;

    public ServerCharacter(Character character) {
        this.character = character;
        pos = Vector2.Zero.cpy();
        velocity = Vector2.Zero.cpy();
        speed = 100f;
    }

    public Character get() {
        return character;
    }

    @Override
    public Vector2 getPos() {
        return pos;
    }

    @Override
    public Vector2 getVelocity() {
        return velocity;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    public int getMovementFlags() {
        return movementFlags;
    }

    public void stopMovement(boolean axis, boolean positive) {
        if (axis) {
            if (positive) movementFlags &= ~RIGHT;
            else movementFlags &= ~LEFT;
        } else {
            if (positive) movementFlags &= ~UP;
            else movementFlags &= ~DOWN;
        }
    }

    public void updateMovement(boolean axis, boolean positive) {
        if (axis) {
            if (positive) movementFlags |= RIGHT;
            else movementFlags |= LEFT;
        } else {
            if (positive) movementFlags |= UP;
            else movementFlags |= DOWN;
        }
    }

    @Override
    public boolean isMoving() {
        return movementFlags != 0 && SidedCharacter.super.isMoving();
    }
}
