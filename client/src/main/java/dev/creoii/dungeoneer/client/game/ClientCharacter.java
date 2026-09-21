package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.render.RenderLayer;
import dev.creoii.dungeoneer.client.render.RenderUtils;
import dev.creoii.dungeoneer.client.render.Renderable;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletType;
import dev.creoii.dungeoneer.definitions.item.inventory.EquipmentInventory;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;
import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.definitions.statuseffect.StatusEffect;
import dev.creoii.dungeoneer.definitions.statuseffect.StatusEffectInstance;
import dev.creoii.dungeoneer.network.c2s.raid.AttackC2S;
import dev.creoii.dungeoneer.util.RemovalReason;
import dev.creoii.dungeoneer.util.VectorUtils;
import dev.creoii.dungeoneer.util.context.Context;
import dev.creoii.dungeoneer.util.action.value.ValueType;
import dev.creoii.dungeoneer.util.collision.MovementCollisionManager;
import dev.creoii.dungeoneer.util.stat.StatContainer;
import dev.creoii.dungeoneer.util.stat.StatUtils;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class ClientCharacter implements Character<ClientRaid>, Renderable {
    private final Context context;
    private final Dungeoneer client;
    @Nullable private CharacterDefinition character;
    private Sprite sprite;
    private final float[] pos;
    private final float[] renderPos;
    private final float[] velocity;
    private final StatContainer stats;
    private EquipmentInventory equipment;
    private final float[] correction;
    private final Rectangle bounds;
    private long lastAttackTime;
    private boolean attackPending;
    private long statusEffects;
    private AnimationState animationState;
    private final Queue<StatusText> statusTexts;
    private boolean dead;

    private int currentAttackId;
    private final Table<Long, Integer, BulletNode<?, ?>> predictedBullets;
    private final PriorityQueue<Long> freeIds;
    private long nextId;

    public ClientCharacter(Dungeoneer client, @Nullable CharacterDefinition character) {
        context = new Context();
        context.set(ValueType.RANDOM, new Random());
        context.set(ValueType.ENTITY, this);
        context.set(ValueType.POSITION, new Vector2());
        this.client = client;
        this.character = character;
        pos = VectorUtils.zero();
        renderPos = VectorUtils.zero();
        velocity = VectorUtils.zero();
        if (character == null) {
            stats = StatContainer.ZERO.copy();
            equipment = EquipmentInventory.createEmpty(this);
            random().setSeed(0);
        } else {
            sprite = new Sprite(client.getAssets().getTexture(Assets.Atlas.CHARACTER, character.characterClass().id()));
            stats = character.characterClass().baseStats().copy();
            equipment = new EquipmentInventory(this, character.characterClass().equipment(), character.equipment());
            random().setSeed(character.id());
            context.set(ValueType.HEALTH, stats.health().value());
        }
        correction = VectorUtils.zero();
        bounds = new Rectangle(0f, 0f, 8f, 8f);
        animationState = AnimationState.IDLE_DOWN;
        statusTexts = new ArrayDeque<>(1);
        dead = false;

        predictedBullets = HashBasedTable.create();
        freeIds = new PriorityQueue<>();
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
        return client.get().getID();
    }

    public Dungeoneer getClient() {
        return client;
    }

    @Override
    public @Nullable CharacterDefinition get() {
        return character;
    }

    public long getLastAttackTime() {
        return lastAttackTime;
    }

    public void set(CharacterDefinition character) {
        this.character = character;
        if (character == null) {
            sprite = null;
            setEquipment(null);
            stats.set(StatContainer.ZERO.copy());
            random().setSeed(0);
        } else {
            sprite = new Sprite(client.getAssets().getTexture(Assets.Atlas.CHARACTER, character.characterClass().id()));
            setEquipment(new EquipmentInventory(this, character.characterClass().equipment(), character.equipment()));
            stats.set(character.characterClass().baseStats());
            random().setSeed(character.id());
            context.set(ValueType.HEALTH, stats.health().value());
        }
    }

    @Override
    public float getCenterX() {
        return getRenderX() + sprite.getWidth() * .5f;
    }

    @Override
    public float getCenterY() {
        return getRenderY() + sprite.getHeight() * .5f;
    }

    public Sprite getSprite() {
        return sprite;
    }

    @Override
    public float[] getPos() {
        return pos;
    }

    public float[] getRenderPos() {
        return renderPos;
    }

    public float getRenderX() {
        return renderPos[0];
    }

    public float getRenderY() {
        return renderPos[1];
    }

    public void setRenderPos(float x, float y) {
        renderPos[0] = x;
        renderPos[1] = y;
    }

    @Override
    public float[] getVelocity() {
        return velocity;
    }

    @Override
    public EquipmentInventory getEquipment() {
        return equipment;
    }

    public void setEquipment(EquipmentInventory equipment) {
        this.equipment = equipment;
    }

    @Override
    public StatContainer getStats() {
        return stats;
    }

    public int nextAttackId() {
        return currentAttackId++;
    }

    @Override
    public int getCurrentAttackId() {
        return currentAttackId;
    }

    @Override
    public void setCurrentAttackId(int currentAttackId) {
        this.currentAttackId = currentAttackId;
    }

    public void freeClientId(long clientId) {
        freeIds.offer(clientId);
    }

    public Table<Long, Integer, BulletNode<?, ?>> getPredictedBullets() {
        return predictedBullets;
    }

    @Override
    public List<BulletNode<?, ?>> attack(ClientRaid raid, int bulletCount, float baseAngle, float arcGap, float angleOffset, float x, float y, float[] mouseDir, BulletType bullet, int indexOffset, int attackIndex) {
        List<BulletNode<?, ?>> bulletNodes = new ArrayList<>();

        for (int i = 0; i < bulletCount; ++i) {
            float angle = (baseAngle + i * arcGap) + angleOffset;

            float radians = angle * MathUtils.degreesToRadians;
            float cos = MathUtils.cos(radians);
            float sin = MathUtils.sin(radians);

            float rotatedX = mouseDir[0] * cos - mouseDir[1] * sin;
            float rotatedY = mouseDir[1] * cos + mouseDir[0] * sin;

            if (willHitWallRightAway(raid.getDungeonMap(), x, y, rotatedX, rotatedY))
                continue;

            int damage = getEquipment().getWeapon().damage().get(context()).intValue();

            bulletNodes.add(raid.createHierarchy(damage, x, y, rotatedX, rotatedY, bullet, i + indexOffset, false, 1));
        }

        if (!bulletNodes.isEmpty()) {
            long clientId = freeIds.isEmpty() ? nextId++ : freeIds.poll();
            bulletNodes.forEach(bulletNode -> {
                predictedBullets.put(clientId, bulletNode.getIndex() - indexOffset, bulletNode); // Standardize bullet index to be zero-based
            });
            if (isLocal()) {
                client.get().sendTCP(new AttackC2S(raid.get().id(), character.accountId(), mouseDir[0], mouseDir[1], clientId, attackIndex, getCurrentAttackId()));
            }
        }

        return bulletNodes;
    }

    @Override
    public boolean addStatusEffect(StatusEffectInstance statusEffect) {
        statusEffect.statusEffect().applier().apply(getRaid(), context);
        statusEffects |= 1L << DataManager.getInternalId(DataManager.SchemaType.STATUS_EFFECT, statusEffect.statusEffect().id());
        return true;
    }

    @Override
    public boolean removeStatusEffect(StatusEffect statusEffect) {
        statusEffect.remover().apply(getRaid(), context);
        statusEffects &= ~(1L << DataManager.getInternalId(DataManager.SchemaType.STATUS_EFFECT, statusEffect.id()));
        return true;
    }

    @Override
    public boolean hasStatusEffect(StatusEffect statusEffect) {
        return (statusEffects & (1L << DataManager.getInternalId(DataManager.SchemaType.STATUS_EFFECT, statusEffect.id()))) != 0;
    }

    public void updateStatusEffects(long add, long remove) {
        long added = add & ~statusEffects;
        long removed = remove & statusEffects;
        statusEffects |= add;
        statusEffects &= ~remove;

        while (added != 0L) {
            int internalId = Long.numberOfTrailingZeros(added);
            long mask = 1L << internalId;

            StatusEffect statusEffect = DataManager.getStatusEffect(internalId);
            if (statusEffect != null) {
                statusEffect.applier().apply(getRaid(), context);
            }
            added &= ~mask;
        }

        while (removed != 0L) {
            int internalId = Long.numberOfTrailingZeros(removed);
            long mask = 1L << internalId;

            StatusEffect statusEffect = DataManager.getStatusEffect(internalId);
            if (statusEffect != null) {
                statusEffect.remover().apply(getRaid(), context);
            }
            removed &= ~mask;
        }
    }

    @Override
    public void clearStatusEffects() {
        statusEffects = 0L;
    }

    @Override
    public boolean damage(int damage) {
        boolean damaged = Character.super.damage(damage);
        if (damaged) {
            statusTexts.add(new StatusText(String.valueOf(damage), Color.RED, 1000, System.currentTimeMillis()));
        }
        return damaged;
    }

    @Override
    public void heal(int amount) {
        Character.super.heal(amount);
        statusTexts.add(new StatusText(String.valueOf(amount), Color.GREEN, 1000, System.currentTimeMillis()));
    }

    @Override
    public void setDead(boolean dead) {
        this.dead = dead;
    }

    @Override
    public boolean isDead() {
        return dead;
    }

    public float[] getCorrection() {
        return correction;
    }

    public void setCorrection(float x, float y) {
        correction[0] = x;
        correction[1] = y;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public ClientRaid getRaid() {
        return client.getState().getCurrentRaid();
    }

    @Override
    public void setRaid(ClientRaid raid) {
    }

    public void setLastAttackTime(long lastAttackTime) {
        this.lastAttackTime = lastAttackTime;
    }

    public boolean isNull() {
        return character == null;
    }

    public void setAttackPending(boolean attackPending) {
        this.attackPending = attackPending;
    }

    public boolean isAttackPending() {
        return attackPending;
    }

    public boolean isLocal() {
        return character.accountId() == client.getState().getAccount().id() && character.id() == client.getState().getActiveCharacter().get().id();
    }

    @Override
    public void die() {
        Character.super.die();
        if (getRaid() != null && !getRaid().isNull() && !isLocal()) {
            getRaid().removeCharacter(character.accountId(), RemovalReason.DEATH);
        }
    }

    public AnimationState getAnimationState() {
        return animationState;
    }

    public void setAnimationState(AnimationState animationState) {
        this.animationState = animationState;
    }

    @Override
    public void updateVelocity(float[] velocity, int movementFlags, float rotation) {
        if (dead) return;

        Character.super.updateVelocity(velocity, movementFlags, rotation);

        AnimationState to;
        if (VectorUtils.isZero(velocity)) {
            to = AnimationState.toIdle(animationState);
        } else if (Math.abs(velocity[1]) >= Math.abs(velocity[0])) {
            to = velocity[1] > 0 ? AnimationState.MOVING_UP : AnimationState.MOVING_DOWN;
        } else to = velocity[0] > 0 ? AnimationState.MOVING_RIGHT : AnimationState.MOVING_LEFT;

        animationState = AnimationState.toMoving(to);
    }

    @Override
    public boolean tick(float dt) {
        if (!inRaid())
            return false;

        if (dead && this == client.getState().getActiveCharacter()) {
            return false;
        }

        float speed = StatUtils.getCalculatedSpeed(this, stats.speed().value());
        float[] target = getTargetPosition(pos, velocity, speed, dt);

        Vector2 modified = MovementCollisionManager.modifyMove(getRaid().getDungeonMap(), this, target[0], target[1], true);
        setPos(modified.x, modified.y);

        correction[0] *= Math.max(0f, 1f - dt);
        correction[1] *= Math.max(0f, 1f - dt);

        float targetX = getX() + correction[0];
        float targetY = getY() + correction[1];

        float alpha = Math.min(1f, 50f * dt);
        setRenderPos(MathUtils.lerp(getRenderX(), targetX, alpha), MathUtils.lerp(getRenderY(), targetY, alpha));

        long toTickEffects = statusEffects;
        while (toTickEffects != 0L) {
            int internalId = Long.numberOfTrailingZeros(toTickEffects);
            long mask = 1L << internalId;

            StatusEffect statusEffect = DataManager.getStatusEffect(internalId);
            if (statusEffect != null) {
                statusEffect.ticker().apply(getRaid(), context);
            }
            toTickEffects &= ~mask;
        }

        statusTexts.removeIf(StatusText::isExpired);

        return true;
    }

    @Override
    public RenderLayer renderLayer() {
        return RenderLayer.OBJECT_OUTLINED;
    }

    @Override
    public void render(Dungeoneer client, PolygonSpriteBatch batch, Camera camera, float rotation, float dt) {
        if (dead) return;

        float[] pos = {getRenderX(), getRenderY()};
        VectorUtils.prj2(pos, 0f, rotation, camera.position.x, camera.position.y);
        sprite.setPosition(pos[0], pos[1]);
        sprite.draw(batch);

        RenderUtils.renderStatusEffects(this, renderPos, sprite.getHeight() * .8f, client, batch);
        // RenderUtils.renderStatusTexts(renderPos, statusTexts, sprite.getHeight(), batch); // TODO: Fix large font
    }

    @Override
    public void renderDebug(ShapeRenderer shapeRenderer, float[] mouseDir) {
        if (dead) return;

        if (isLocal()) {
            shapeRenderer.setColor(isAttackPending() ? Color.GREEN : Color.WHITE);
            shapeRenderer.line(getCenterX(), getCenterY(), getCenterX() + mouseDir[0] * 32f, getCenterY() + mouseDir[1] * 32f);
        }

        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(getRenderX(), getRenderY(), bounds.width, bounds.height);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Override
    public float depth(float rotation, OrthographicCamera camera) {
        return ClientDungeonMap.project(getRenderX(), getRenderY(), 0f, rotation, camera.position.x, camera.position.y).y;
    }
}
