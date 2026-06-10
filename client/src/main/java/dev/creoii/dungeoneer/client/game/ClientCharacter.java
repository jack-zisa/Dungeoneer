package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.definitions.Character;
import dev.creoii.dungeoneer.definitions.sided.SidedCharacter;
import dev.creoii.dungeoneer.util.stat.StatContainer;
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
                character.characterClass().baseStats().speed().value()
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
        } else {
            sprite = new Sprite(client.getAssets().getTexture(Assets.Atlas.CHARACTER, character.characterClass().id()));
            stats.setHealth(character.characterClass().baseStats().health().value());
            stats.setSpeed(character.characterClass().baseStats().speed().value());
        }
    }

    @Nullable
    public TiledMapTile getTileOn(TiledMapTileLayer layer) {
        int tileX = (int) ((getRenderPos().x + 4f) / 8f);
        int tileY = (int) ((getRenderPos().y + 4f) / 8f);

        TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
        return cell != null ? cell.getTile() : null;
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

    public boolean isNull() {
        return character == null;
    }
}
