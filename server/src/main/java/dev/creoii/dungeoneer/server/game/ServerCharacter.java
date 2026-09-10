package dev.creoii.dungeoneer.server.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.network.s2c.character.CharacterMoveS2C;
import dev.creoii.dungeoneer.network.s2c.raid.MoveRaidCharactersS2C;
import dev.creoii.dungeoneer.util.VectorUtils;
import dev.creoii.dungeoneer.util.collision.MovementCollisionManager;
import dev.creoii.dungeoneer.util.stat.StatContainer;
import dev.creoii.dungeoneer.util.stat.StatUtils;

public class ServerCharacter implements Character {
    private final int connectionId;
    private final CharacterDefinition character;
    private final float[] pos;
    private final float[] velocity;
    private final Rectangle bounds;
    private final StatContainer stats;
    private final StatContainer maxStats;
    private long lastAttackTime;
    private boolean dead;

    public ServerCharacter(int connectionId, CharacterDefinition character) {
        this.connectionId = connectionId;
        this.character = character;
        pos = VectorUtils.zero();
        velocity = VectorUtils.zero();
        bounds = new Rectangle(0f, 0f, 8f, 8f);
        stats = new StatContainer(
            character.characterClass().baseStats().health().value(),
            character.characterClass().baseStats().speed().value(),
            character.characterClass().baseStats().attackSpeed().value()
        );
        maxStats = new StatContainer(
            character.characterClass().maxStats().health().value(),
            character.characterClass().maxStats().speed().value(),
            character.characterClass().maxStats().attackSpeed().value()
        );
        dead = false;
    }

    @Override
    public int getConnectionId() {
        return connectionId;
    }

    @Override
    public CharacterDefinition get() {
        return character;
    }

    @Override
    public float[] getPos() {
        return pos;
    }

    @Override
    public float getCenterX() {
        return getX() + bounds.width / 2f;
    }

    @Override
    public float getCenterY() {
        return getY() + bounds.height / 2f;
    }

    @Override
    public float[] getVelocity() {
        return velocity;
    }

    @Override
    public Rectangle getBounds() {
        bounds.setPosition(getX(), getY());
        return bounds;
    }

    @Override
    public StatContainer getStats() {
        return stats;
    }

    @Override
    public StatContainer getMaxStats() {
        return maxStats;
    }

    @Override
    public void setDead(boolean dead) {
        this.dead = dead;
    }

    @Override
    public boolean isDead() {
        return dead;
    }

    public long getLastAttackTime() {
        return lastAttackTime;
    }

    public void setLastAttackTime(long lastAttackTime) {
        this.lastAttackTime = lastAttackTime;
    }

    public void tick(ServerRaid raid, float dt) {
        if (isMoving() && !dead) {
            // Update character position
            float speed = StatUtils.getCalculatedSpeed(stats.speed().value());
            float[] target = getTargetPosition(pos, velocity, speed, dt);

            Vector2 modified = MovementCollisionManager.modifyMove(raid.getDungeonMap(), this, target[0], target[1], true);
            setPos(modified.x, modified.y);

            // Sync character movement
            raid.getMoveEntries().add(new MoveRaidCharactersS2C.Entry(character.accountId(), character.id(), getX(), getY()));
            raid.getServer().get().sendToUDP(connectionId, new CharacterMoveS2C(character.id(), getX(), getY()));
        }
    }
}
