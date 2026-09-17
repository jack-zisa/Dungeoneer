package dev.creoii.dungeoneer.definitions.attack.bullet;

import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;
import dev.creoii.dungeoneer.definitions.sided.Entity;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.util.EntityOwnable;

public abstract class Bullet<R extends Raid<?, ?, ?, ?>> extends BulletNode<SingleBulletType, R> implements Pool.Poolable, EntityOwnable {
    private int damage;
    private boolean enemy;

    private Entity<?> owner;

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
    public Entity<?> getOwner() {
        return owner;
    }

    @Override
    public void setOwner(Entity<?> owner) {
        this.owner = owner;
    }

    @Override
    public void reset() {
        super.reset();
        damage = 0;
        enemy = false;
    }
}
