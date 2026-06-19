package dev.creoii.dungeoneer.server.game;

import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.network.s2c.character.CharacterMoveS2C;
import dev.creoii.dungeoneer.server.DungeoneerServer;
import dev.creoii.dungeoneer.util.VectorUtils;
import dev.creoii.dungeoneer.util.stat.StatContainer;
import dev.creoii.dungeoneer.util.stat.StatUtils;

public class ServerCharacter implements Character {
    private final int connectionId;
    private final CharacterDefinition character;
    private final float[] pos;
    private final float[] velocity;
    private final StatContainer stats;
    private long lastAttackTime;

    public ServerCharacter(int connectionId, CharacterDefinition character) {
        this.connectionId = connectionId;
        this.character = character;
        pos = VectorUtils.zero();
        velocity = VectorUtils.zero();
        stats = new StatContainer(
            character.characterClass().baseStats().health().value(),
            character.characterClass().baseStats().speed().value(),
            character.characterClass().baseStats().attackSpeed().value()
        );
    }

    public int getConnectionId() {
        return connectionId;
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

    public void tick(DungeoneerServer server, float dt) {
        // Update character position
        float speed = StatUtils.getCalculatedSpeed(stats.speed().value());
        updatePosition(pos, velocity, speed, dt);

        // Sync character movement
        server.get().sendToUDP(connectionId, new CharacterMoveS2C(character.id(), getX(), getY()));
    }
}
