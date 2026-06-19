package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.attack.*;
import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.network.c2s.raid.AttackC2S;
import dev.creoii.dungeoneer.util.VectorUtils;
import dev.creoii.dungeoneer.util.stat.StatContainer;
import org.jspecify.annotations.Nullable;

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

        pos = VectorUtils.zero();
        renderPos = VectorUtils.zero();
        velocity = VectorUtils.zero();
        if (character == null) {
            stats = StatContainer.ZERO.copy();
        } else {
            stats = new StatContainer(
                character.characterClass().baseStats().health().value(),
                character.characterClass().baseStats().speed().value(),
                character.characterClass().baseStats().attackSpeed().value()
            );
        }
        correction = VectorUtils.zero();
        animationState = AnimationState.IDLE_DOWN;
    }

    public Dungeoneer getClient() {
        return client;
    }

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

    @Override
    public boolean attack(Attack attack, Raid raid, float[] mouseDir) {
        if (Character.super.attack(attack, raid, mouseDir)) {
            client.get().sendTCP(new AttackC2S(raid.get().id(), character.accountId(), mouseDir[0], mouseDir[1]));
        }
        return false;
    }
}
