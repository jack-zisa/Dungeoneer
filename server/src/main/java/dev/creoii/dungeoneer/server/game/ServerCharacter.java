package dev.creoii.dungeoneer.server.game;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.item.inventory.EquipmentInventory;
import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.definitions.statuseffect.StatusEffect;
import dev.creoii.dungeoneer.definitions.statuseffect.StatusEffectInstance;
import dev.creoii.dungeoneer.network.s2c.character.CharacterMoveS2C;
import dev.creoii.dungeoneer.network.s2c.raid.DamageCharactersS2C;
import dev.creoii.dungeoneer.network.s2c.raid.MoveCharactersS2C;
import dev.creoii.dungeoneer.network.s2c.raid.StatusEffectsS2C;
import dev.creoii.dungeoneer.util.VectorUtils;
import dev.creoii.dungeoneer.util.context.Context;
import dev.creoii.dungeoneer.util.action.value.ValueType;
import dev.creoii.dungeoneer.util.collision.MovementCollisionManager;
import dev.creoii.dungeoneer.util.event.MoveEvents;
import dev.creoii.dungeoneer.util.stat.StatContainer;
import dev.creoii.dungeoneer.util.stat.StatUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ServerCharacter implements Character<ServerRaid> {
    private final Context context;
    private final int connectionId;
    private final CharacterDefinition character;
    private final float[] pos;
    private final float[] velocity;
    private final Rectangle bounds;
    private final StatContainer stats;
    private final EquipmentInventory equipment;
    @Nullable private ServerRaid raid;
    private long lastAttackTime;
    private final Long2ObjectArrayMap<StatusEffectInstance> statusEffects;
    private long pendingStatusEffectAdds;
    private long pendingStatusEffectRemoves;
    private final List<StatusEffect> expiredEffects;
    private boolean dead;
    private int currentAttackId;

    public ServerCharacter(int connectionId, CharacterDefinition character) {
        this.connectionId = connectionId;
        this.character = character;
        pos = VectorUtils.zero();
        velocity = VectorUtils.zero();
        bounds = new Rectangle(0f, 0f, 8f, 8f);
        stats = character.characterClass().baseStats().copy();
        equipment = new EquipmentInventory(this, character.characterClass().equipment(), character.equipment()); // Need to init after stats as stat bonuses will apply
        raid = null;
        statusEffects = new Long2ObjectArrayMap<>();
        expiredEffects = new ArrayList<>();
        dead = false;

        context = new Context();
        context.set(ValueType.RANDOM, new Random(character.id()));
        context.set(ValueType.ENTITY, this);
        context.set(ValueType.HEALTH, stats.health().value());
        context.set(ValueType.POSITION, new Vector2(pos[0], pos[1]));
    }

    @Override
    public Context context() {
        return context;
    }

    @Override
    public Random random() {
        return context.get(ValueType.RANDOM);
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
        return bounds;
    }

    @Override
    public EquipmentInventory getEquipment() {
        return equipment;
    }

    @Override
    public StatContainer getStats() {
        return stats;
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
        long removeMask = pendingStatusEffectAdds;
        for (var entry : statusEffects.long2ObjectEntrySet()) {
            long internalId = entry.getLongKey();
            entry.getValue().statusEffect().remover().apply(raid, context);
            removeMask |= 1L << internalId;
        }

        pendingStatusEffectAdds = 0L;
        pendingStatusEffectRemoves |= removeMask;
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

    @Override
    public int getCurrentAttackId() {
        return currentAttackId;
    }

    @Override
    public void setCurrentAttackId(int currentAttackId) {
        this.currentAttackId = currentAttackId;
    }

    public long getLastAttackTime() {
        return lastAttackTime;
    }

    public void setLastAttackTime(long lastAttackTime) {
        this.lastAttackTime = lastAttackTime;
    }

    @Override
    public boolean damage(int damage) {
        if (Character.super.damage(damage)) {
            if (!dead) raid.getDamageEntries().add(new DamageCharactersS2C.Entry(character.accountId(), damage));
            return true;
        }
        return false;
    }

    @Override
    public boolean tick(float dt) {
        if (dead) {
            return false;
        }

        if (inRaid()) {
            if (isMoving()) {
                if (!MoveEvents.PRE.invoker().onPreMove(this, raid))
                    return true;

                // Get target position
                float speed = StatUtils.getCalculatedSpeed(this, stats.speed().value());
                float[] target = getTargetPosition(pos, velocity, speed, dt);

                // Handle tile collision
                Vector2 modified = MovementCollisionManager.modifyMove(raid.getDungeonMap(), this, target[0], target[1], true);
                setPos(modified.x, modified.y);

                // Sync character movement
                raid.getMoveEntries().add(new MoveCharactersS2C.Entry(character.accountId(), character.id(), getX(), getY()));
                raid.getServer().get().sendToUDP(connectionId, new CharacterMoveS2C(character.id(), getX(), getY()));

                MoveEvents.POST.invoker().onPostMove(this, raid);
            }

            if (hasPendingStatusEffectChanges()) {
                raid.getStatusEffectEntries().add(new StatusEffectsS2C.Entry(character.accountId(), pendingStatusEffectAdds, pendingStatusEffectRemoves));
                pendingStatusEffectAdds = 0L;
                pendingStatusEffectRemoves = 0L;
            }
        }

        float regeneration = StatUtils.getCalculatedVitality(this, stats.vitality().value());
        heal((int) (regeneration * dt));

        statusEffects.values().forEach(instance -> {
            if (instance.isExpired()) {
                expiredEffects.add(instance.statusEffect());
            } else instance.statusEffect().ticker().apply(raid, context);
        });
        expiredEffects.forEach(this::removeStatusEffect);
        expiredEffects.clear();

        return true;
    }
}
