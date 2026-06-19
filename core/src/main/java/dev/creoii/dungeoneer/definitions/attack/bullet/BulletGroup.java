package dev.creoii.dungeoneer.definitions.attack.bullet;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;

public class BulletGroup extends BulletNode<GroupBulletType> implements Pool.Poolable {
    private final Array<BulletNode<?>> children;

    public BulletGroup() {
        children = new Array<>();
    }

    public Array<BulletNode<?>> getChildren() {
        return children;
    }

    public void addChild(BulletNode<?> child) {
        children.add(child);
    }

    @Override
    public float getCenterX() {
        return getX();
    }

    @Override
    public float getCenterY() {
        return getY();
    }

    @Override
    public void reset() {
        super.reset();
        children.clear();
    }

    @Override
    public boolean update(float dt) {
        if (!super.update(dt))
            return false;

        for (BulletNode<?> child : children) {
            if (!child.update(dt))
                return false;
        }
        return true;
    }

    @Override
    public void applyTransform(float originX, float originY, float dirX, float dirY) {
        super.applyTransform(originX, originY, dirX, dirY);
        for (BulletNode<?> child : children) {
            child.applyTransform(getX(), getY(), getLocalDirX(), getLocalDirY());
        }
    }
}
