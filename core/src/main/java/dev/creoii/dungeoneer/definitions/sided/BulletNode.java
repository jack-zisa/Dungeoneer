package dev.creoii.dungeoneer.definitions.sided;

import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletType;
import dev.creoii.dungeoneer.definitions.attack.bullet.path.BulletPathType;
import org.jspecify.annotations.Nullable;

public abstract class BulletNode implements Pool.Poolable {
    private BulletType type;
    private BulletPathType.Instance<?> path;
    private float x;
    private float y;
    private float startX;
    private float startY;
    private float dirX;
    private float dirY;
    private float offsetX;
    private float offsetY;
    private float speed;
    private float distanceTravelled;
    private float lifetime;
    private int index;
    private float age;
    @Nullable BulletNode parent;

    public BulletType getType() {
        return type;
    }

    public BulletPathType.Instance<?> getPath() {
        return path;
    }

    public void setType(BulletType type) {
        this.type = type;
        path = type.path().create();
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setPos(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getStartX() {
        return startX;
    }

    public float getStartY() {
        return startY;
    }

    public void setStartPos(float x, float y) {
        startX = x;
        startY = y;
    }

    public float getDirX() {
        return dirX;
    }

    public float getDirY() {
        return dirY;
    }

    public void setDirection(float x, float y) {
        dirX = x;
        dirY = y;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffset(float x, float y) {
        offsetX = x;
        offsetY = y;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getDistanceTravelled() {
        return distanceTravelled;
    }

    public void resetDistanceTravelled() {
        distanceTravelled = 0f;
    }

    public float getLifetime() {
        return lifetime;
    }

    public void setLifetime(float lifetime) {
        this.lifetime = lifetime;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public float getAge() {
        return age;
    }

    public @Nullable BulletNode getParent() {
        return parent;
    }

    public void setParent(@Nullable BulletNode parent) {
        this.parent = parent;
    }

    public boolean update(float dt) {
        if ((lifetime -= dt) <= 0f) {
            return false;
        }

        age += dt;

        speed += type.acceleration() * dt;
        distanceTravelled += speed * dt;

        path.update(this, dt);
        return true;
    }

    @Override
    public void reset() {
        setPos(0f, 0f);
        setStartPos(0f, 0f);
        setDirection(0f, 0f);
        setOffset(0f, 0f);
        type = null;
        path = null;
        lifetime = 0f;
        age = 0f;
        index = 0;
        startX = 0f;
        startY = 0f;
        distanceTravelled = 0f;
        speed = 0f;
        parent = null;
    }
}
