package dev.creoii.dungeoneer.server.game;

import com.badlogic.gdx.math.Vector2;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.stat.StatContainer;

public class ServerCharacter implements Character {
    private final CharacterDefinition character;
    private final float[] pos;
    private final float[] velocity;
    private final StatContainer stats;
    private long lastAttackTime;
    private boolean attacking;

    public ServerCharacter(CharacterDefinition character) {
        this.character = character;
        pos = new float[]{0f, 0f};
        velocity = new float[]{0f, 0f};
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
        return 0; // TODO: Implement collision box
    }

    @Override
    public float getCenterY() {
        return 0; // TODO: Implement collision box
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

    @Override
    public boolean isAttacking() {
        return attacking;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
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

        if (velocity[0] != 0f || velocity[1] != 0f) {
            float len = Vector2.len(velocity[0], velocity[1]);
            if (len != 0) {
                velocity[0] /= len;
                velocity[1] /= len;
            }
        }
    }
}
