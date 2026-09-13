package dev.creoii.dungeoneer.definitions.attack.bullet;

import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;
import dev.creoii.dungeoneer.definitions.sided.Raid;

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
    public void reset() {
        super.reset();
        damage = 0;
        enemy = false;
    }
}
