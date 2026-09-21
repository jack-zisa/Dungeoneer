package dev.creoii.dungeoneer.definitions.attack.bullet;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.util.collision.MovementCollisionManager;

public abstract class BulletGroup<R extends Raid<?, ?, ?, ?>> extends BulletNode<GroupBulletType, R> implements Pool.Poolable {
    private final Array<BulletNode<?, ?>> children;

    public BulletGroup() {
        children = new Array<>();
    }

    public Array<BulletNode<?, ?>> getChildren() {
        return children;
    }

    public void addChild(BulletNode<?, ?> child) {
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
    public boolean tick(float dt) {
        if (!super.tick(dt)) {
            children.forEach(bulletNode -> {
                if (!bulletNode.isDead()) {
                    bulletNode.setParent(null);
                }
            });
            return false;
        }

        for (int i = children.size - 1; i >= 0; --i) {
            BulletNode<?, ?> child = children.get(i);
            if (!child.tick(dt)) {
                children.removeIndex(i);
            }
        }

        return true;
    }

    @Override
    public boolean applyTransform(float dt) {
        float rotationSpeed = getType().rotationSpeed();
        if (rotationSpeed != 0f)
            incrementAngle(rotationSpeed * dt);
        return super.applyTransform(dt);
    }
}
