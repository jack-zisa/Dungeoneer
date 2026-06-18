package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.util.VectorUtils;
import org.jspecify.annotations.Nullable;

public class ClientLaser implements Pool.Poolable {
    private final Vector2 pos;
    private final Vector2 direction;
    private float angleOffset;
    private float width;
    private float length;
    private float lifetime;
    @Nullable private ClientCharacter attached;

    public ClientLaser() {
        this.pos = new Vector2();
        this.direction = new Vector2();
    }

    public Vector2 getPos() {
        return pos;
    }

    public void setPos(float x, float y) {
        pos.set(x, y);
    }

    public Vector2 getDirection() {
        return direction;
    }

    public void setAngleOffset(float angleOffset) {
        this.angleOffset = angleOffset;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public void setLifetime(float lifetime) {
        this.lifetime = lifetime;
    }

    public void setAttached(@Nullable ClientCharacter attached) {
        this.attached = attached;
    }

    @Override
    public void reset() {
        pos.set(0f, 0f);
        direction.set(0f, 0f);
        angleOffset = 0f;
        width = 0f;
        length = 0f;
        lifetime = 0f;
        attached = null;
    }

    private static final float[] TEST = VectorUtils.zero(); // TODO: Hook up

    public boolean update(float dt) {
        if (attached == null) {
            return (lifetime -= dt) > 0f;
        } else {
            pos.set(attached.getCenterX(), attached.getCenterY());

            float[] mouseDir = TEST; //attached.getClient().getInputListener().getDirectionToMouse(attached.getCenterX(), attached.getCenterY());
            direction.set(mouseDir[0], mouseDir[1]).rotateDeg(angleOffset);

            return Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        }
    }
}
