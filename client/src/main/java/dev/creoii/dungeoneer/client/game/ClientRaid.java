package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.Bullet;
import dev.creoii.dungeoneer.definitions.Raid;
import org.jspecify.annotations.Nullable;

public class ClientRaid {
    private Raid raid;
    private final ClientDungeonMap dungeon;

    private final Pool<ClientBullet> bulletPool = new Pool<>() {
        @Override
        protected ClientBullet newObject() {
            return new ClientBullet();
        }
    };
    private final Array<ClientBullet> bullets = new Array<>();

    public ClientRaid(Raid raid) {
        this.raid = raid;
        dungeon = new ClientDungeonMap();
    }

    public Raid get() {
        return raid;
    }

    public void set(@Nullable Raid raid) {
        this.raid = raid;
    }

    public Pool<ClientBullet> getBulletPool() {
        return bulletPool;
    }

    public Array<ClientBullet> getBullets() {
        return bullets;
    }

    public ClientDungeonMap getDungeon() {
        return dungeon;
    }

    public boolean isNull() {
        return raid == null;
    }

    public void update(float dt) {
        for (int i = bullets.size - 1; i >= 0; --i) {
            ClientBullet bullet = bullets.get(i);

            if (!bullet.update(dt)) {
                bullets.removeIndex(i);
                bulletPool.free(bullet);
            }
        }
    }

    public void end() {
        bulletPool.freeAll(bullets);
        bullets.removeRange(0, bullets.size - 1);
    }

    public void addBullet(float x, float y, float dirX, float dirY, Bullet bullet, int index) {
        ClientBullet poolBullet = bulletPool.obtain();
        poolBullet.set(bullet);
        poolBullet.setStartPos(x, y);
        poolBullet.setPos(x, y);
        poolBullet.setAngleOffset(bullet.angleOffset());
        poolBullet.setSpeed(bullet.speed());
        poolBullet.setDirection(dirX, dirY);
        poolBullet.setLifetime(bullet.lifetime());
        poolBullet.setIndex(index);
        poolBullet.setOrbitPhase(MathUtils.PI2 * index / 5f);
        bullets.add(poolBullet);
    }
}
