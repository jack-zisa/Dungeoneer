package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.definitions.Character;
import dev.creoii.dungeoneer.definitions.sided.SidedCharacter;
import dev.creoii.dungeoneer.network.c2s.raid.AttackC2S;
import dev.creoii.dungeoneer.util.stat.StatContainer;
import dev.creoii.dungeoneer.util.stat.StatUtils;
import org.jspecify.annotations.Nullable;

public class ClientCharacter implements SidedCharacter {
    private final Dungeoneer client;
    @Nullable private Character character;
    private Sprite sprite;
    private final Vector2 pos;
    private final Vector2 renderPos;
    private final Vector2 velocity;
    private final StatContainer stats;
    private final Vector2 correction;
    private long lastAttackTime;
    private boolean attackPending;

    public ClientCharacter(Dungeoneer client, @Nullable Character character) {
        this.client = client;
        this.character = character;

        if (character == null) sprite = null;
        else sprite = new Sprite(client.getAssets().getTexture(Assets.Atlas.CHARACTER, character.characterClass().id()));

        pos = new Vector2();
        renderPos = new Vector2();
        velocity = new Vector2();
        if (character == null) {
            stats = StatContainer.ZERO.copy();
        } else {
            stats = new StatContainer(
                character.characterClass().baseStats().health().value(),
                character.characterClass().baseStats().speed().value(),
                character.characterClass().baseStats().attackSpeed().value()
            );
        }
        correction = new Vector2();
    }

    public @Nullable Character get() {
        return character;
    }

    public void set(@Nullable Character character) {
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
        int tileX = (int) (getRenderPos().x + sprite.getWidth() * .625f); // .5f * .125f
        int tileY = (int) ((getRenderPos().y) * .125f);

        TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
        return cell != null ? cell.getTile() : null;
    }

    public float getCenterX() {
        return getRenderPos().x + sprite.getWidth() * .5f;
    }

    public float getCenterY() {
        return getRenderPos().y + sprite.getHeight() * .5f;
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

            float arcGap = 20f;
            int bulletCount = 6;

            float baseAngle = -arcGap * (bulletCount - 1) / 2f;
            float angleOffset = 0f;

            Vector2 mouseDir = client.getInputListener().getDirectionToMouse(getCenterX(), getCenterY());

            for (int i = 0; i < bulletCount; ++i) {
                float angle = (baseAngle + i * arcGap) + angleOffset;

                float radians = angle * MathUtils.degreesToRadians;
                float cos = MathUtils.cos(radians);
                float sin = MathUtils.sin(radians);

                float rotatedX = mouseDir.x * cos - mouseDir.y * sin;
                float rotatedY = mouseDir.x * sin + mouseDir.y * cos;

                client.getState().getCurrentRaid().addBullet(getCenterX(), getCenterY(), rotatedX, rotatedY, -45f, 0f, -50f, 60f, 3.5f, 10f, 10f, 0f, 0f, i + 1);
            }
            client.get().sendTCP(new AttackC2S(client.getState().getCurrentRaid().get().id(), character.accountId()));
        }
    }

    public Sprite getSprite() {
        return sprite;
    }

    @Override
    public Vector2 getPos() {
        return pos;
    }

    public Vector2 getRenderPos() {
        return renderPos;
    }

    @Override
    public Vector2 getVelocity() {
        return velocity;
    }

    @Override
    public StatContainer getStats() {
        return stats;
    }

    public Vector2 getCorrection() {
        return correction;
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
}
