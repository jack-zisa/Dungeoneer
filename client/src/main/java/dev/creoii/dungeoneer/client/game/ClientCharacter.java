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
    private final Vector2 velocity;
    private float speed;

    public ClientCharacter(@Nullable Character character) {
        this.character = character;

        if (character == null) sprite = null;
        else sprite = new Sprite(AssetManager.getClassTexture(character.characterClass().id()));

        pos = Vector2.Zero.cpy();
        velocity = Vector2.Zero.cpy();
        speed = 100f;
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

    @Override
    public Vector2 getVelocity() {
        return velocity;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    public boolean isNull() {
        return character == null;
    }
}
