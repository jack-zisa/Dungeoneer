package dev.creoii.dungeoneer.definitions.attack.bullet;

import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;

public class Bullet extends BulletNode<SingleBulletType> implements Pool.Poolable {
    @Override
    public float getCenterX() {
        return getX() + getType().scale() * 4f;
    }

    @Override
    public float getCenterY() {
        return getY() + getType().scale() * 4f;
    }
}
