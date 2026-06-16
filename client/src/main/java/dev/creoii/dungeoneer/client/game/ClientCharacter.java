package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.attack.*;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletDefinition;
import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.network.c2s.raid.AttackC2S;
import dev.creoii.dungeoneer.util.stat.StatContainer;
import dev.creoii.dungeoneer.util.stat.StatUtils;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ClientCharacter implements Character {
    private final Dungeoneer client;
    @Nullable private CharacterDefinition character;
    private Sprite sprite;
    private final float[] pos;
    private final float[] renderPos;
    private final float[] velocity;
    private final StatContainer stats;
    private final float[] correction;
    private long lastAttackTime;
    private boolean attackPending;
    private AnimationState animationState;

    public ClientCharacter(Dungeoneer client, @Nullable CharacterDefinition character) {
        this.client = client;
        this.character = character;

        if (character == null) sprite = null;
        else sprite = new Sprite(client.getAssets().getTexture(Assets.Atlas.CHARACTER, character.characterClass().id()));

        pos = new float[]{0f, 0f};
        renderPos = new float[]{0f, 0f};
        velocity = new float[]{0f, 0f};
        if (character == null) {
            stats = StatContainer.ZERO.copy();
        } else {
            stats = new StatContainer(
                character.characterClass().baseStats().health().value(),
                character.characterClass().baseStats().speed().value(),
                character.characterClass().baseStats().attackSpeed().value()
            );
        }
        correction = new float[]{0f, 0f};
        animationState = AnimationState.IDLE;
    }

    public Dungeoneer getClient() {
        return client;
    }

    public @Nullable CharacterDefinition get() {
        return character;
    }

    public void set(@Nullable CharacterDefinition character) {
        this.character = character;
        if (character == null) {
            sprite = null;
            stats.setHealth(0);
            stats.setSpeed(0);
            stats.setAttackSpeed(0);
        } else {
            sprite = new Sprite(client.getAssets().getTexture(Assets.Atlas.CHARACTER, character.characterClass().id()));
            stats.setHealth(character.characterClass().baseStats().health().value());
            stats.setSpeed(character.characterClass().baseStats().speed().value());
            stats.setAttackSpeed(character.characterClass().baseStats().attackSpeed().value());
        }
    }

    @Nullable
    public TiledMapTile getTileOn(TiledMapTileLayer layer) {
        int tileX = (int) (getRenderX() + sprite.getWidth() * .625f); // .5f * .125f
        int tileY = (int) (getRenderY() * .125f);

        TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
        return cell != null ? cell.getTile() : null;
    }

    @Override
    public float getCenterX() {
        return getRenderX() + sprite.getWidth() * .5f;
    }

    @Override
    public float getCenterY() {
        return getRenderY() + sprite.getHeight() * .5f;
    }

    public void update(float dt) {
        long currentTime = System.currentTimeMillis();
        long cooldown = (long) StatUtils.getCalculatedAttackSpeed(stats.attackSpeed().value());

        if (client.getState().getStatus() == ClientState.Status.RAIDING
            && !attackPending
            && !client.getState().getCurrentRaid().isNull()
            && (currentTime - lastAttackTime) >= cooldown
            && Gdx.input.isButtonPressed(Input.Buttons.LEFT)
        ) {
            attackPending = true;

            Attack attack = DataManager.getAttack("simple");
            attack(attack);
        }
    }

    public void attack(Attack attack) {
        switch (attack) {
            case ReferenceAttack(String id, _) -> attack(DataManager.getAttack(id));
            case BulletAttack(_, _, int bulletCount, float arcGap, float angleOffset, Vector2 offset, int indexOffset) -> {
                BulletDefinition bullet = DataManager.getBullet("dark_magic");
                if (bullet == null)
                    return;

                float baseAngle = -arcGap * (bulletCount - 1) / 2f;
                Vector2 mouseDir = client.getInputListener().getDirectionToMouse(getCenterX(), getCenterY());

                Vector2 up = new Vector2(-mouseDir.y, mouseDir.x);
                float x = getCenterX() + mouseDir.x * offset.x + up.x * offset.y;
                float y = getCenterY() + mouseDir.y * offset.x + up.y * offset.y;

                for (int i = 0; i < bulletCount; ++i) {
                    float angle = (baseAngle + i * arcGap) + angleOffset;

                    float radians = angle * MathUtils.degreesToRadians;
                    float cos = MathUtils.cos(radians);
                    float sin = MathUtils.sin(radians);

                    float rotatedX = mouseDir.x * cos - mouseDir.y * sin;
                    float rotatedY = mouseDir.x * sin + mouseDir.y * cos;

                    client.getState().getCurrentRaid().addBullet(x, y, rotatedX, rotatedY, bullet, i + indexOffset, this);
                }
                client.get().sendTCP(new AttackC2S(client.getState().getCurrentRaid().get().id(), character.accountId()));
            }
            case LaserAttack(_, _, Vector2 size, int laserCount, float arcGap, float angleOffset, float lifetime, boolean attached) -> {
                float baseAngle = -arcGap * (laserCount - 1) / 2f;
                for (int i = 0; i < laserCount; ++i) {
                    float angle = (baseAngle + i * arcGap) + angleOffset;
                    client.getState().getCurrentRaid().addLaser(getCenterX(), getCenterY(), angle, size.x, size.y, lifetime, attached ? this : null);
                }
                client.get().sendTCP(new AttackC2S(client.getState().getCurrentRaid().get().id(), character.accountId()));
            }
            case CompositeAttack(_, _, List<Attack> attacks) -> attacks.forEach(this::attack);
            case null, default -> throw new IllegalStateException("Unexpected attack value: " + attack);
        }
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

    public float[] getCorrection() {
        return correction;
    }

    public void setCorrection(float x, float y) {
        correction[0] = x;
        correction[1] = y;
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

    public enum AnimationState {
        IDLE,
        ATTACKING_UP,
        ATTACKING_DOWN,
        ATTACKING_LEFT,
        ATTACKING_RIGHT,
        MOVING_UP,
        MOVING_DOWN,
        MOVING_LEFT,
        MOVING_RIGHT
    }
}
