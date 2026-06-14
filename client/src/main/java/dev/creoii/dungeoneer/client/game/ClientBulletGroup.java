package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;

public class ClientBulletGroup extends BulletNode implements Pool.Poolable {
    private final Array<BulletNode> children;

    public ClientBulletGroup() {
        children = new Array<>();
    }

    public Array<BulletNode> getChildren() {
        return children;
    }

    public void addChild(BulletNode child) {
        children.add(child);
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

        for (BulletNode child : children) {
            if (!child.update(dt))
                return false;
        }

        return true;
    }

    @Override
    public void resolveTransform() {
        super.resolveTransform();

        for (BulletNode child : children) {
            child.resolveTransform();
        }
    }

    @Override
    public void applyTransform(float originX, float originY, float dirX, float dirY) {
        super.applyTransform(originX, originY, dirX, dirY);

        for (BulletNode child : children) {
            child.applyTransform(
                getX(),
                getY(),
                child.getDirX(),
                child.getDirY()
            );
        }
    }
}
