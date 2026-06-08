package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import dev.creoii.dungeoneer.client.AssetManager;
import dev.creoii.dungeoneer.definitions.Character;
import dev.creoii.dungeoneer.definitions.sided.SidedCharacter;
import org.jspecify.annotations.Nullable;

public class ClientCharacter implements SidedCharacter {
    @Nullable
    private Character character;
    private Sprite sprite;
    private final Vector2 pos;
    private final Vector2 renderPos;
    private final Vector2 velocity;
    private float speed;
    private final Vector2 correction;

    public ClientCharacter(@Nullable Character character) {
        this.character = character;

        if (character == null) sprite = null;
        else sprite = new Sprite(AssetManager.getClassTexture(character.characterClass().id()));

        pos = new Vector2();
        renderPos = new Vector2();
        velocity = new Vector2();
        speed = 100f;
        correction = new Vector2();
    }

    public @Nullable Character get() {
        return character;
    }

    public void set(@Nullable Character character) {
        this.character = character;
        if (character == null) sprite = null;
        else sprite = new Sprite(AssetManager.getClassTexture(character.characterClass().id()));
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
    public float getSpeed() {
        return speed;
    }

    public Vector2 getCorrection() {
        return correction;
    }

    public boolean isNull() {
        return character == null;
    }
}
