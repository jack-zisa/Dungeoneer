package dev.creoii.dungeoneer.server.game;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.definitions.statuseffect.StatusEffect;
import dev.creoii.dungeoneer.definitions.statuseffect.StatusEffectInstance;
import dev.creoii.dungeoneer.network.s2c.character.CharacterMoveS2C;
import dev.creoii.dungeoneer.network.s2c.raid.DamageCharactersS2C;
import dev.creoii.dungeoneer.network.s2c.raid.MoveRaidCharactersS2C;
import dev.creoii.dungeoneer.network.s2c.raid.StatusEffectsS2C;
import dev.creoii.dungeoneer.util.VectorUtils;
import dev.creoii.dungeoneer.util.action.Context;
import dev.creoii.dungeoneer.util.action.value.ValueType;
import dev.creoii.dungeoneer.util.collision.MovementCollisionManager;
import dev.creoii.dungeoneer.util.stat.StatContainer;
import dev.creoii.dungeoneer.util.stat.StatUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ServerCharacter implements Character<ServerRaid> {
    private final int connectionId;
    private final CharacterDefinition character;
    private final float[] pos;
    private final float[] velocity;
    private final Rectangle bounds;
    private final StatContainer stats;
    private final StatContainer maxStats;
    @Nullable private ServerRaid raid;
    private long lastAttackTime;
    private final Long2ObjectArrayMap<StatusEffectInstance> statusEffects;
    private long pendingStatusEffectAdds;
    private long pendingStatusEffectRemoves;
    private final List<StatusEffect> expiredEffects;
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
        raid = null;
        statusEffects = new Long2ObjectArrayMap<>();
        expiredEffects = new ArrayList<>();
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

    public boolean hasPendingStatusEffectChanges() {
        return pendingStatusEffectAdds != 0L || pendingStatusEffectRemoves != 0L;
    }

    @Override
    public boolean addStatusEffect(StatusEffectInstance instance) {
        long internalId = DataManager.getInternalId(DataManager.SchemaType.STATUS_EFFECT, instance.statusEffect().id());
        StatusEffectInstance previous = statusEffects.put(internalId, instance);
        if (previous != null) {
            return false;
        }

        Context context = new Context()
            .set(ValueType.CHARACTER, this)
            .set(ValueType.HEALTH, stats.health().value());

        long mask = 1L << internalId;
        pendingStatusEffectAdds |= mask;
        pendingStatusEffectRemoves &= ~mask;

        instance.statusEffect().applier().apply(raid, context);

        return true;
    }

    @Override
    public boolean removeStatusEffect(StatusEffect statusEffect) {
        long internalId = DataManager.getInternalId(DataManager.SchemaType.STATUS_EFFECT, statusEffect.id());
        StatusEffectInstance removed = statusEffects.remove(internalId);
        if (removed == null)
            return false;

        Context context = new Context() // TODO: Add Contextual interface to cache Context at any level
            .set(ValueType.CHARACTER, this)
            .set(ValueType.HEALTH, stats.health().value());

        long mask = 1L << internalId;
        pendingStatusEffectRemoves |= mask;
        pendingStatusEffectAdds &= ~mask;

        removed.statusEffect().remover().apply(raid, context);
        return true;
    }

    @Override
    public boolean hasStatusEffect(StatusEffect statusEffect) {
        return statusEffects.containsKey(DataManager.getInternalId(DataManager.SchemaType.STATUS_EFFECT, statusEffect.id()));
    }

    @Override
    public void clearStatusEffects() {
        pendingStatusEffectRemoves = 0L;
        pendingStatusEffectAdds = 0L;
        statusEffects.clear();
    }

    @Override
    public ServerRaid getRaid() {
        return raid;
    }

    @Override
    public void setRaid(ServerRaid raid) {
        this.raid = raid;
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

    @Override
    public void damage(int damage) {
        Character.super.damage(damage);
        raid.getDamageEntries().add(new DamageCharactersS2C.Entry(character.accountId(), damage));
    }

    @Override
    public void tick(float dt) {
        if (dead) {
            return;
        }

        if (isMoving() && !dead && inRaid()) {
            // Update character position
            float speed = StatUtils.getCalculatedSpeed(stats.speed().value());
            float[] target = getTargetPosition(pos, velocity, speed, dt);

            // Handle tile collision
            Vector2 modified = MovementCollisionManager.modifyMove(raid.getDungeonMap(), this, target[0], target[1], true);
            setPos(modified.x, modified.y);

            // Sync character movement
            raid.getMoveEntries().add(new MoveRaidCharactersS2C.Entry(character.accountId(), character.id(), getX(), getY()));
            raid.getServer().get().sendToUDP(connectionId, new CharacterMoveS2C(character.id(), getX(), getY()));

            if (hasPendingStatusEffectChanges()) {
                raid.getStatusEffectEntries().add(new StatusEffectsS2C.Entry(character.accountId(), pendingStatusEffectAdds, pendingStatusEffectRemoves));
                pendingStatusEffectAdds = 0L;
                pendingStatusEffectRemoves = 0L;
            }
        }

        Context context = new Context() // TODO: Add Contextual interface to cache Context at any level
            .set(ValueType.CHARACTER, this)
            .set(ValueType.HEALTH, stats.health().value());

        statusEffects.values().forEach(instance -> {
            instance.statusEffect().ticker().apply(raid, context);

            if (instance.isExpired()) {
                expiredEffects.add(instance.statusEffect());
            }
        });
        expiredEffects.forEach(this::removeStatusEffect);
        expiredEffects.clear();
    }
}
