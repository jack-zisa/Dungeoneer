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
            child.update(dt);

            float perpX = -getDirY();
            float perpY = getDirX();

            float offsetWorldX = getDirX() * child.getOffsetY() + perpX * child.getOffsetX();
            float offsetWorldY = getDirY() * child.getOffsetY() + perpY * child.getOffsetX();

            child.setPos(getX() + offsetWorldX + child.getLocalX(), getY() + offsetWorldY + child.getLocalY());
        }
        return true;
    }
}
