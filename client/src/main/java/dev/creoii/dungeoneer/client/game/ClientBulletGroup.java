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
            child.setPos(getX() + child.getOffsetX(), getY() + child.getOffsetY());

            if (child instanceof ClientBulletGroup group) {
                group.update(dt);
            }
        }
        return true;
    }
}
