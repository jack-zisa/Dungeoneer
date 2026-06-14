package dev.creoii.dungeoneer.definitions.sided;

import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletType;
import dev.creoii.dungeoneer.definitions.attack.bullet.path.BulletPathType;
import dev.creoii.dungeoneer.definitions.attack.bullet.path.OrbitBulletPathType;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

public abstract class BulletNode implements Pool.Poolable {
    private BulletType type;
    private BulletPathType.Instance<?> path;
    private final float[] pos;
    private final float[] startPos;
    private final float[] direction;
    private final float[] startDirection;
    private final float[] localPos;
    private final float[] offset;
    private final float[] formationOffset;
    private float speed;
    private float distanceTravelled;
    private float lifetime;
    private int index;
    private float age;
    @Nullable private BulletNode parent;

    public BulletNode() {
        pos = new float[]{0f, 0f};
        startPos = new float[]{0f, 0f};
        direction = new float[]{0f, 0f};
        startDirection = new float[]{0f, 0f};
        localPos = new float[]{0f, 0f};
        offset = new float[]{0f, 0f};
        formationOffset = new float[]{0f, 0f};
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

    public float getStartDirX() {
        return direction[0];
    }

    public float getStartDirY() {
        return direction[1];
    }

    public void setStartDirection(float x, float y) {
        startDirection[0] = x;
        startDirection[1] = y;
        setDirection(x, y);
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

    public float getFormationX() {
        return formationOffset[0];
    }

    public float getFormationY() {
        return formationOffset[1];
    }

    public void setFormationOffset(float x, float y) {
        formationOffset[0] = x;
        formationOffset[1] = y;
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
        if ((lifetime -= dt) <= 0f)
            return false;

        age += dt;

        speed += type.acceleration() * dt;
        distanceTravelled += speed * dt;

        float[] offset = path.getOffset2(age);

        this.offset[0] = offset[0];
        this.offset[1] = offset[1];

        return true;
    }

    public void resolveTransform() {
        if (parent == null) {
            applyTransform(
                startPos[0],
                startPos[1],
                getDirX(),
                getDirY()
            );
        }
    }

    public void applyTransform(
        float originX,
        float originY,
        float dirX,
        float dirY
    ) {
        float perpX = -dirY;
        float perpY = dirX;

        // Rotate local path offset into world space
        float pathWorldX =
            dirX * offset[1]
                + perpX * offset[0];

        float pathWorldY =
            dirY * offset[1]
                + perpY * offset[0];

        float formationWorldX =
            dirX * formationOffset[1]
                + perpX * formationOffset[0];

        float formationWorldY =
            dirY * formationOffset[1]
                + perpY * formationOffset[0];

        setPos(
            originX + formationWorldX + pathWorldX,
            originY + formationWorldY + pathWorldY
        );
    }

    @Override
    public void reset() {
        setPos(0f, 0f);
        setStartPos(0f, 0f);
        setDirection(0f, 0f);
        setStartDirection(0f, 0f);
        setOffset(0f, 0f);
        setFormationOffset(0f, 0f);
        type = null;
        path = null;
        lifetime = 0f;
        age = 0f;
        index = 0;
        distanceTravelled = 0f;
        speed = 0f;
        parent = null;
    }
}
