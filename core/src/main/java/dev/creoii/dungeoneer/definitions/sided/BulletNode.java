package dev.creoii.dungeoneer.definitions.sided;

import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletType;
import dev.creoii.dungeoneer.definitions.attack.bullet.path.BulletPathType;
import org.jspecify.annotations.Nullable;

public abstract class BulletNode implements Pool.Poolable {
    private BulletType type;
    private BulletPathType.Instance<?> path;
    private final float[] pos;
    private final float[] startPos;
    private final float[] localPos;
    private final float[] direction;
    private final float[] localDirection;
    private final float[] offset;
    private float speed;
    private float distanceTravelled;
    private float lifetime;
    private int index;
    private float age;
    private float angle;
    @Nullable private BulletNode parent;

    public BulletNode() {
        pos = new float[]{0f, 0f};
        startPos = new float[]{0f, 0f};
        localPos = new float[]{0f, 0f};
        direction = new float[]{0f, 0f};
        localDirection = new float[]{0f, 0f};
        offset = new float[]{0f, 0f};
    }

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
        return pos[0];
    }

    public float getY() {
        return pos[1];
    }

    public void setPos(float x, float y) {
        pos[0] = x;
        pos[1] = y;
    }

    public float getStartX() {
        return startPos[0];
    }

    public float getStartY() {
        return startPos[1];
    }

    public void setStartPos(float x, float y) {
        startPos[0] = x;
        startPos[1] = y;
        setPos(x, y);
    }

    public float getLocalX() {
        return localPos[0];
    }

    public float getLocalY() {
        return localPos[1];
    }

    public void setLocalPos(float x, float y) {
        localPos[0] = x;
        localPos[1] = y;
    }

    public float getDirX() {
        return direction[0];
    }

    public float getDirY() {
        return direction[1];
    }

    public void setDirection(float x, float y) {
        direction[0] = x;
        direction[1] = y;
    }

    public float getLocalDirX() {
        return direction[0];
    }

    public float getLocalDirY() {
        return direction[1];
    }

    public void setLocalDirection(float x, float y) {
        localDirection[0] = x;
        localDirection[1] = y;
    }

    public float getOffsetX() {
        return offset[0];
    }

    public float getOffsetY() {
        return offset[1];
    }

    public void setOffset(float x, float y) {
        offset[0] = x;
        offset[1] = y;
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

    public float getAngle() {
        return angle;
    }

    public void incrementAngle(float f) {
        angle += f;
    }

    public @Nullable BulletNode getParent() {
        return parent;
    }

    public void setParent(@Nullable BulletNode parent) {
        this.parent = parent;
    }

    public boolean update(float dt) {
        if ((lifetime -= dt) <= 0f)
            return false;

        age += dt;

        speed += type.acceleration() * dt;
        distanceTravelled += speed * dt;

        float[] pathOffset = path.getOffset(this, distanceTravelled);

        localPos[0] = pathOffset[0] + offset[0];
        localPos[1] = pathOffset[1] + offset[1];

        return true;
    }

    public void applyTransform(float originX, float originY, float dirX, float dirY) {
        float perpX = -dirY;
        float perpY = dirX;

        float worldOffsetX = dirX * localPos[1] + perpX * localPos[0];
        float worldOffsetY = dirY * localPos[1] + perpY * localPos[0];

        setPos(originX + worldOffsetX, originY + worldOffsetY);

        localDirection[0] = dirX;
        localDirection[1] = dirY;
    }

    @Override
    public void reset() {
        setStartPos(0f, 0f);
        setLocalPos(0f, 0f);
        setDirection(0f, 0f);
        setLocalDirection(0f, 0f);
        setOffset(0f, 0f);
        type = null;
        path = null;
        lifetime = 0f;
        age = 0f;
        index = 0;
        distanceTravelled = 0f;
        speed = 0f;
        angle = 0f;
        parent = null;
    }
}
