package dev.creoii.dungeoneer.server.game;

import com.badlogic.gdx.math.Vector2;
import dev.creoii.dungeoneer.definitions.Character;
import dev.creoii.dungeoneer.definitions.sided.SidedCharacter;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.stat.StatContainer;

public class ServerCharacter implements SidedCharacter {
    private final Character character;
    private final Vector2 pos;
    private final Vector2 velocity;
    private final StatContainer stats;
    private long lastAttackTime;

    public ServerCharacter(Character character) {
        this.character = character;
        pos = new Vector2();
        velocity = new Vector2();
        stats = new StatContainer(
            character.characterClass().baseStats().health().value(),
            character.characterClass().baseStats().speed().value(),
            character.characterClass().baseStats().attackSpeed().value()
        );
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
    public StatContainer getStats() {
        return stats;
    }

    public long getLastAttackTime() {
        return lastAttackTime;
    }

    public void setLastAttackTime(long lastAttackTime) {
        this.lastAttackTime = lastAttackTime;
    }

    public void updateMovement(int movementFlags) {
        float dx = 0;
        float dy = 0;

        if ((movementFlags & Constants.CHARACTER_MOVEMENT_FLAG_LEFT) != 0) dx -= 1f;
        if ((movementFlags & Constants.CHARACTER_MOVEMENT_FLAG_RIGHT) != 0) dx += 1f;
        if ((movementFlags & Constants.CHARACTER_MOVEMENT_FLAG_UP) != 0) dy += 1f;
        if ((movementFlags & Constants.CHARACTER_MOVEMENT_FLAG_DOWN) != 0) dy -= 1f;

        velocity.set(dx, dy);

        if (!velocity.isZero()) velocity.nor();
    }
}
