package dev.creoii.dungeoneer.definitions.attack.bullet;

import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;

public class Bullet extends BulletNode<SingleBulletType> implements Pool.Poolable {
    private boolean enemy;

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
    public void reset() {
        super.reset();
        enemy = false;
    }
}
