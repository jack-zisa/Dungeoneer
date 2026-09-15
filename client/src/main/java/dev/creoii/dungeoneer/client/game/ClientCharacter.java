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
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.render.RenderLayer;
import dev.creoii.dungeoneer.client.render.RenderUtils;
import dev.creoii.dungeoneer.client.render.Renderable;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.item.inventory.EquipmentInventory;
import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.definitions.statuseffect.StatusEffect;
import dev.creoii.dungeoneer.definitions.statuseffect.StatusEffectInstance;
import dev.creoii.dungeoneer.util.RemovalReason;
import dev.creoii.dungeoneer.util.Tickable;
import dev.creoii.dungeoneer.util.VectorUtils;
import dev.creoii.dungeoneer.util.Context;
import dev.creoii.dungeoneer.util.action.value.ValueType;
import dev.creoii.dungeoneer.util.collision.MovementCollisionManager;
import dev.creoii.dungeoneer.util.stat.StatContainer;
import dev.creoii.dungeoneer.util.stat.StatUtils;
import org.jspecify.annotations.Nullable;

import java.util.Random;

public class ClientCharacter implements Character<ClientRaid>, Renderable, Tickable {
    private final Random random;
    private final Dungeoneer client;
    @Nullable private CharacterDefinition character;
    private Sprite sprite;
    private final float[] pos;
    private final float[] renderPos;
    private final float[] velocity;
    private final StatContainer stats;
    private final StatContainer maxStats;
    private EquipmentInventory equipment;
    private final float[] correction;
    private final Rectangle bounds;
    private long lastAttackTime;
    private boolean attackPending;
    private long statusEffects;
    private AnimationState animationState;
    private boolean dead;

    public ClientCharacter(Dungeoneer client, @Nullable CharacterDefinition character) {
        this.random = new Random();
        this.client = client;
        this.character = character;
        pos = VectorUtils.zero();
        renderPos = VectorUtils.zero();
        velocity = VectorUtils.zero();
        if (character == null) {
            stats = StatContainer.ZERO.copy();
            maxStats = StatContainer.ZERO.copy();
            equipment = EquipmentInventory.createEmpty(this);
            random.setSeed(0);
        } else {
            sprite = new Sprite(client.getAssets().getTexture(Assets.Atlas.CHARACTER, character.characterClass().id()));
            stats = character.characterClass().baseStats().copy();
            maxStats = character.characterClass().maxStats().copy();
            equipment = new EquipmentInventory(this, character.characterClass().equipment(), character.equipment());
            random.setSeed(character.id());
        }
        correction = VectorUtils.zero();
        bounds = new Rectangle(0f, 0f, 8f, 8f);
        animationState = AnimationState.IDLE_DOWN;
        dead = false;
    }

    @Override
    public Random random() {
        return Dungeoneer.RANDOM;
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
            maxStats.set(StatContainer.ZERO.copy());
            random.setSeed(0);
        } else {
            sprite = new Sprite(client.getAssets().getTexture(Assets.Atlas.CHARACTER, character.characterClass().id()));
            setEquipment(new EquipmentInventory(this, character.characterClass().equipment(), character.equipment()));
            stats.set(character.characterClass().baseStats());
            maxStats.set(character.characterClass().maxStats());
            random.setSeed(character.id());
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

    @Override
    public StatContainer getMaxStats() {
        return maxStats;
    }

    @Override
    public boolean addStatusEffect(StatusEffectInstance statusEffect) {
        Context context = new Context()
            .set(ValueType.CHARACTER, this)
            .set(ValueType.HEALTH, stats.health().value());
        statusEffect.statusEffect().applier().apply(getRaid(), context);
        statusEffects |= 1L << DataManager.getInternalId(DataManager.SchemaType.STATUS_EFFECT, statusEffect.statusEffect().id());
        return true;
    }

    @Override
    public boolean removeStatusEffect(StatusEffect statusEffect) {
        Context context = new Context()
            .set(ValueType.CHARACTER, this)
            .set(ValueType.HEALTH, stats.health().value());
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

        Context context = new Context()
            .set(ValueType.CHARACTER, this)
            .set(ValueType.HEALTH, stats.health().value());

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
        bounds.setPosition(getX(), getY());
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
        return character.id() == client.getState().getActiveCharacter().get().id();
    }

    @Override
    public void die() {
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

        Context context = new Context() // TODO: Add Contextual interface to cache Context at any level
            .set(ValueType.CHARACTER, this)
            .set(ValueType.HEALTH, stats.health().value());

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

        return true;
    }

    @Override
    public RenderLayer renderLayer() {
        return RenderLayer.OBJECT_OUTLINED;
    }

    @Override
    public void render(Dungeoneer client, PolygonSpriteBatch batch, Camera camera, float rotation, float dt) {
        if (dead) return;

        float cx = getRenderX() + sprite.getWidth() * .5f;
        float cy = getRenderY() + sprite.getHeight() * .5f;
        float[] pos = {cx, cy};
        VectorUtils.prj2(pos, 0f, rotation, camera.position.x, camera.position.y);
        sprite.setPosition(pos[0] - sprite.getWidth() * .5f, pos[1] - sprite.getHeight() * .5f);
        sprite.draw(batch);

        RenderUtils.renderStatusEffects(this, renderPos, sprite.getHeight() * .8f, client, batch);
    }

    @Override
    public void renderDebug(ShapeRenderer shapeRenderer, float[] mouseDir) {
        if (dead) return;

        shapeRenderer.setColor(isAttackPending() ? Color.GREEN : Color.WHITE);
        shapeRenderer.line(getCenterX(), getCenterY(), getCenterX() + mouseDir[0] * 32f, getCenterY() + mouseDir[1] * 32f);

        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(getRenderX(), getRenderY(), bounds.width, bounds.height);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Override
    public float depth(float rotation, OrthographicCamera camera) {
        return ClientDungeonMap.project(getRenderX() + sprite.getWidth() * .5f, getRenderY() + sprite.getHeight() * .5f, 0f, rotation, camera.position.x, camera.position.y).y;
    }
}
