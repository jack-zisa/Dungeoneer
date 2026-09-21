package dev.creoii.dungeoneer.definitions.attack.bullet;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.util.collision.MovementCollisionManager;

public abstract class Bullet<R extends Raid<?, ?, ?, ?>> extends BulletNode<SingleBulletType, R> implements Pool.Poolable {
    private int damage;
    private boolean enemy;

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }

    public void setEnemy(boolean enemy) {
        this.enemy = enemy;
    }

    public boolean isEnemy() {
        return enemy;
    }

    @Override
    public float getCenterX() {
        return getX() + getType().scale() * 4f;
    }

    @Override
    public float getCenterY() {
        return getY() + getType().scale() * 4f;
    }

    @Override
    public boolean applyTransform(float dt) {
        float[] pathOffset = getPath().getOffset(this, getDistanceTravelled());

        float originX;
        float originY;
        if (getParent() instanceof BulletGroup<?> group) {
            float localX = pathOffset[0] + getOffsetX();
            float localY = pathOffset[1] + getOffsetY();

            float rotation = getParent() != null ? group.getAngle() : 0f;
            float cos = MathUtils.cosDeg(rotation);
            float sin = MathUtils.sinDeg(rotation);

            setLocalPos(localX * cos - localY * sin, localX * sin + localY * cos);
        } else setLocalPos(pathOffset[0] + getOffsetX(), pathOffset[1] + getOffsetY());

        originX = getParent() != null ? getParent().getX() : getStartX();
        originY = getParent() != null ? getParent().getY() : getStartY();

        float dirX = getParent() != null ? getParent().getDirX() : getDirX();
        float dirY = getParent() != null ? getParent().getDirY() : getDirY();

        float perpX = -dirY;
        float perpY = dirX;

        float pathX = dirX * getLocalY() + perpX * getLocalX();
        float pathY = dirY * getLocalY() + perpY * getLocalX();

        float targetX = originX + pathX + getOffsetX();
        float targetY = originY + pathY + getOffsetY();

        Vector2 modified = MovementCollisionManager.modifyMove(getRaid().getDungeonMap(), this, targetX, targetY);

        float previousX = getX();
        float previousY = getY();

        setPos(modified.x, modified.y);

        float movementX = getX() - previousX;
        float movementY = getY() - previousY;

        float length2 = movementX * movementX + movementY * movementY;
        if (length2 > .000001f) {
            float invLength = 1f / (float) Math.sqrt(length2);
            setLocalDirection(movementX * invLength, movementY * invLength);
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        super.reset();
        damage = 0;
        enemy = false;
    }
}
