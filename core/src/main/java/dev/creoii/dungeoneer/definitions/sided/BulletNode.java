package dev.creoii.dungeoneer.definitions.sided;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletType;
import dev.creoii.dungeoneer.definitions.attack.bullet.SingleBulletType;
import dev.creoii.dungeoneer.definitions.attack.bullet.path.BulletPathType;
import dev.creoii.dungeoneer.util.VectorUtils;
import dev.creoii.dungeoneer.util.action.value.ValueType;
import dev.creoii.dungeoneer.util.collision.MovementCollisionManager;
import dev.creoii.dungeoneer.util.context.Context;
import org.jspecify.annotations.Nullable;

public abstract class BulletNode<T extends BulletType, R extends Raid<?, ?, ?, ?>> implements Entity<R>, Pool.Poolable {
    private long id;
    private final Context context;
    private T type;
    private BulletPathType.Instance<?> path;
    private final float[] pos;
    private final float[] startPos;
    private final float[] localPos;
    private final float[] direction;
    private final float[] startDirection;
    private final float[] localDirection;
    private final float[] offset;
    private float speed, minSpeed, maxSpeed;
    private float distanceTravelled;
    private float lifetime;
    private int index;
    private float age;
    private float angle;
    private boolean dead;
    private final Rectangle bounds;
    @Nullable private BulletNode<?, ?> parent;

    public BulletNode() {
        id = -1L;
        context = new Context();
        context.set(ValueType.ENTITY, this);
        context.set(ValueType.POSITION, new Vector2());
        pos = VectorUtils.zero();
        startPos = VectorUtils.zero();
        localPos = VectorUtils.zero();
        direction = VectorUtils.zero();
        startDirection = VectorUtils.zero();
        localDirection = VectorUtils.zero();
        offset = VectorUtils.zero();
        setDead(false);
        bounds = new Rectangle(0f, 0f, 0f, 0f);
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public Context context() {
        return context;
    }

    public T getType() {
        return type;
    }

    public BulletPathType.Instance<?> getPath() {
        return path;
    }

    public void setType(T type) {
        this.type = type;
        path = type.path().create();

        if (type instanceof SingleBulletType singleBulletType) {
            bounds.setSize(8f * singleBulletType.scale(), 8f * singleBulletType.scale());
        }
    }

    @Override
    public float[] getPos() {
        return pos;
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

    public float getStartDirX() {
        return startDirection[0];
    }

    public float getStartDirY() {
        return startDirection[1];
    }

    public void setStartDirection(float x, float y) {
        startDirection[0] = x;
        startDirection[1] = y;
        setDirection(x, y);
    }

    public float getLocalDirX() {
        return localDirection[0];
    }

    public float getLocalDirY() {
        return localDirection[1];
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

    public void setMinSpeed(float minSpeed) {
        this.minSpeed = minSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public float getMinSpeed() {
        return minSpeed;
    }

    public float getMaxSpeed() {
        return maxSpeed;
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

    public @Nullable BulletNode<?, ?> getParent() {
        return parent;
    }

    public void setParent(@Nullable BulletNode<?, ?> parent) {
        this.parent = parent;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public void setDead(boolean dead) {
        this.dead = dead;
    }

    @Override
    public boolean isDead() {
        return dead;
    }

    @Override
    public void onTileCollision() {
        die();
    }

    @Override
    public boolean tick(float dt) {
        if (isDead() || (lifetime -= dt) <= 0f)
            return false;

        age += dt;

        speed += type.acceleration() * dt;
        speed = MathUtils.clamp(speed, minSpeed, maxSpeed);
        distanceTravelled += speed * dt;

        float[] pathOffset = path.getOffset(this, distanceTravelled);

        localPos[0] = pathOffset[0] + offset[0];
        localPos[1] = pathOffset[1] + offset[1];

        applyTransform(getRaid().getDungeonMap(), getStartX(), getStartY(), getDirX(), getDirY());

        return true;
    }

    public boolean applyTransform(DungeonMap map, float originX, float originY, float dirX, float dirY) {
        float perpX = -dirY;
        float perpY = dirX;

        float worldOffsetX = dirX * localPos[1] + perpX * localPos[0];
        float worldOffsetY = dirY * localPos[1] + perpY * localPos[0];

        float targetX = originX + worldOffsetX;
        float targetY = originY + worldOffsetY;

        Vector2 modified = MovementCollisionManager.modifyMove(map, this, targetX, targetY);

        float previousX = pos[0];
        float previousY = pos[1];

        setPos(modified.x, modified.y);

        float movementX = pos[0] - previousX;
        float movementY = pos[1] - previousY;

        float length2 = movementX * movementX + movementY * movementY;
        if (length2 > .000001f) {
            float invLength = 1f / (float) Math.sqrt(length2);
            localDirection[0] = movementX * invLength;
            localDirection[1] = movementY * invLength;
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        id = -1L;
        context.removeExcept(ValueType.ENTITY);
        setStartPos(0f, 0f);
        setLocalPos(0f, 0f);
        setStartDirection(0f, 0f);
        setLocalDirection(0f, 0f);
        setOffset(0f, 0f);
        type = null;
        path = null;
        lifetime = 0f;
        age = 0f;
        index = 0;
        distanceTravelled = 0f;
        speed = 0f;
        minSpeed = 0f;
        maxSpeed = 0f;
        angle = 0f;
        parent = null;
        setDead(false);
        bounds.set(0f, 0f, 0f, 0f);
    }
}
