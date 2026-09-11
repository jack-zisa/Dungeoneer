package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.Gdx;
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
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.render.RenderLayer;
import dev.creoii.dungeoneer.client.render.Renderable;
import dev.creoii.dungeoneer.client.render.screen.game.DeathScreen;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.definitions.statuseffect.StatusEffect;
import dev.creoii.dungeoneer.definitions.statuseffect.StatusEffectInstance;
import dev.creoii.dungeoneer.network.c2s.character.CharacterDieC2S;
import dev.creoii.dungeoneer.util.VectorUtils;
import dev.creoii.dungeoneer.util.collision.MovementCollisionManager;
import dev.creoii.dungeoneer.util.stat.StatContainer;
import dev.creoii.dungeoneer.util.stat.StatUtils;
import org.jspecify.annotations.Nullable;

public class ClientCharacter implements Character<ClientRaid>, Renderable {
    private final Dungeoneer client;
    @Nullable private CharacterDefinition character;
    private Sprite sprite;
    private final float[] pos;
    private final float[] renderPos;
    private final float[] velocity;
    private final StatContainer stats;
    private final StatContainer maxStats;
    private final float[] correction;
    private final Rectangle bounds;
    private long lastAttackTime;
    private boolean attackPending;
    private long statusEffects;
    private AnimationState animationState;
    private boolean dead;

    public ClientCharacter(Dungeoneer client, @Nullable CharacterDefinition character) {
        this.client = client;
        this.character = character;

        if (character == null) sprite = null;
        else sprite = new Sprite(client.getAssets().getTexture(Assets.Atlas.CHARACTER, character.characterClass().id()));

        pos = VectorUtils.zero();
        renderPos = VectorUtils.zero();
        velocity = VectorUtils.zero();
        if (character == null) {
            stats = StatContainer.ZERO.copy();
            maxStats = StatContainer.ZERO.copy();
        } else {
            stats = character.characterClass().baseStats().copy();
            maxStats = character.characterClass().maxStats().copy();
        }
        correction = VectorUtils.zero();
        bounds = new Rectangle(0f, 0f, 8f, 8f);
        animationState = AnimationState.IDLE_DOWN;
        dead = false;
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

    public void set(@Nullable CharacterDefinition character) {
        this.character = character;
        if (character == null) {
            sprite = null;
            stats.setHealth(0);
            stats.setSpeed(0);
            stats.setAttackSpeed(0);
            maxStats.setHealth(0);
            maxStats.setSpeed(0);
            maxStats.setAttackSpeed(0);
        } else {
            sprite = new Sprite(client.getAssets().getTexture(Assets.Atlas.CHARACTER, character.characterClass().id()));
            stats.set(character.characterClass().baseStats());
            maxStats.set(character.characterClass().maxStats());
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
    public StatContainer getStats() {
        return stats;
    }

    @Override
    public StatContainer getMaxStats() {
        return maxStats;
    }

    @Override
    public boolean addStatusEffect(StatusEffectInstance statusEffect) {
        statusEffects |= 1L << DataManager.getInternalId(DataManager.SchemaType.STATUS_EFFECT, statusEffect.statusEffect().id());
        return true;
    }

    @Override
    public boolean removeStatusEffect(StatusEffect statusEffect) {
        statusEffects &= ~(1L << DataManager.getInternalId(DataManager.SchemaType.STATUS_EFFECT, statusEffect.id()));
        return true;
    }

    @Override
    public boolean hasStatusEffect(StatusEffect statusEffect) {
        return (statusEffects & (1L << DataManager.getInternalId(DataManager.SchemaType.STATUS_EFFECT, statusEffect.id()))) != 0;
    }

    public void updateStatusEffects(long add, long remove) {
        statusEffects |= add;
        statusEffects &= ~remove;
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
    public void tick(float dt) {
        if (!inRaid())
            return;

        if (dead && this == client.getState().getActiveCharacter()) {
            getRaid().setStatus(Raid.Status.END);
            client.getState().setStatus(ClientState.Status.RAID_END);
            client.get().sendTCP(new CharacterDieC2S(getRaid().get().id(), character.id()));
            Gdx.app.postRunnable(() -> client.setScreen(new DeathScreen(client)));
            return;
        }

        float speed = StatUtils.getCalculatedSpeed(stats.speed().value());
        float[] target = getTargetPosition(pos, velocity, speed, dt);

        Vector2 modified = MovementCollisionManager.modifyMove(getRaid().getDungeonMap(), this, target[0], target[1], true);
        setPos(modified.x, modified.y);

        correction[0] *= Math.max(0f, 1f - dt);
        correction[1] *= Math.max(0f, 1f - dt);

        float targetX = getX() + correction[0];
        float targetY = getY() + correction[1];

        float alpha = Math.min(1f, 50f * dt);
        setRenderPos(MathUtils.lerp(getRenderX(), targetX, alpha), MathUtils.lerp(getRenderY(), targetY, alpha));
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
