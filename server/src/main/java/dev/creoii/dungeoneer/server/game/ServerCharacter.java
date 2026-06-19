package dev.creoii.dungeoneer.server.game;

import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.VectorUtils;
import dev.creoii.dungeoneer.util.stat.StatContainer;

public class ServerCharacter implements Character {
    private final CharacterDefinition character;
    private final float[] pos;
    private final float[] velocity;
    private final StatContainer stats;
    private long lastAttackTime;

    public ServerCharacter(CharacterDefinition character) {
        this.character = character;
        pos = VectorUtils.zero();
        velocity = VectorUtils.zero();
        stats = new StatContainer(
            character.characterClass().baseStats().health().value(),
            character.characterClass().baseStats().speed().value(),
            character.characterClass().baseStats().attackSpeed().value()
        );
    }

    public CharacterDefinition get() {
        return character;
    }

    @Override
    public float[] getPos() {
        return pos;
    }

    @Override
    public float getCenterX() {
        return getX() + 4f; // TODO: Implement collision box
    }

    @Override
    public float getCenterY() {
        return getY() + 4f; // TODO: Implement collision box
    }

    @Override
    public float[] getVelocity() {
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

        velocity[0] = dx;
        velocity[1] = dy;

        if (!VectorUtils.isZero(velocity)) {
            VectorUtils.nor(velocity);
        }
    }
}
