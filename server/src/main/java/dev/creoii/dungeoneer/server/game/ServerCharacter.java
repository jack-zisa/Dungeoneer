package dev.creoii.dungeoneer.server.game;

import com.badlogic.gdx.math.Vector2;
import dev.creoii.dungeoneer.definitions.Character;
import dev.creoii.dungeoneer.definitions.sided.SidedCharacter;

public class ServerCharacter implements SidedCharacter {
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int UP = 4;
    public static final int DOWN = 8;

    private final Character character;
    private final Vector2 pos;
    private final Vector2 velocity;
    private float speed;

    public ServerCharacter(Character character) {
        this.character = character;
        pos = new Vector2();
        velocity = new Vector2();
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

    public void updateMovement(int movementFlags) {
        float dx = 0;
        float dy = 0;

        if ((movementFlags & LEFT) != 0) dx -= 1f;
        if ((movementFlags & RIGHT) != 0) dx += 1f;
        if ((movementFlags & UP) != 0) dy += 1f;
        if ((movementFlags & DOWN) != 0) dy -= 1f;

        velocity.set(dx, dy);

        if (!velocity.isZero()) velocity.nor();
    }
}
