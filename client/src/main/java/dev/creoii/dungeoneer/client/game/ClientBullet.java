package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletDefinition;
import dev.creoii.dungeoneer.definitions.sided.SidedBullet;
import org.jspecify.annotations.Nullable;

public class ClientBullet implements SidedBullet, Pool.Poolable {
    private BulletDefinition definition;
    private final Vector2 pos;
    private float startX;
    private float startY;
    private final Vector2 direction;
    private float speed;
    private float distanceTravelled;
    private float lifetime;
    private int index;
    private float age;
    private float orbitPhase;
    private float angleOffset;
    private float angle;
    @Nullable
    private ClientCharacter attached;

    public ClientBullet() {
        pos = new Vector2();
        direction = new Vector2();
        reset();
    }

    public BulletDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(BulletDefinition definition) {
        this.definition = definition;
    }

    public Vector2 getPos() {
        return pos;
    }

    public void setPos(float x, float y) {
        pos.set(x, y);
    }

    @Override
    public float getStartX() {
        return startX;
    }

    @Override
    public float getStartY() {
        return startY;
    }

    public void setStartPos(float x, float y) {
        startX = x;
        startY = y;
    }

    public float getAngleOffset() {
        return angleOffset;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngleOffset(float angleOffset) {
        this.angleOffset = angleOffset;
    }

    public void incrementAngle(float f) {
        angle += f;
    }

    public void setDirection(float x, float y) {
        direction.set(x, y);
    }

    public Vector2 getDirection() {
        return direction;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public void incrementSpeed(float f) {
        speed += f;
    }

    @Override
    public float getDistanceTravelled() {
        return distanceTravelled;
    }

    @Override
    public void incrementDistanceTravelled(float f) {
        distanceTravelled += f;
    }

    @Override
    public float getLifetime() {
        return lifetime;
    }

    public void setLifetime(float lifetime) {
        this.lifetime = lifetime;
    }

    @Override
    public float getOrbitPhase() {
        return orbitPhase;
    }

    public void setOrbitPhase(float orbitPhase) {
        this.orbitPhase = orbitPhase;
    }

    @Override
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public float getAge() {
        return age;
    }

    @Override
    public @Nullable ClientCharacter getAttached() {
        return attached;
    }

    public void setAttached(@Nullable ClientCharacter attached) {
        this.attached = attached;
    }

    @Override
    public void reset() {
        setPos(0f, 0f);
        setDirection(0f, 0f);
        definition = null;
        lifetime = 0f;
        age = 0f;
        index = 0;
        startX = 0f;
        startY = 0f;
        distanceTravelled = 0f;
        speed = 0f;
        orbitPhase = 0f;
        angle = 0f;
        attached = null;
    }

    public boolean update(float dt) {
        if ((lifetime -= dt) <= 0f) {
            return false;
        }

        age += dt;

        definition.path().apply(this, dt);
        return true;
    }
}
