package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletDefinition;
import dev.creoii.dungeoneer.definitions.attack.bullet.SingleBulletDefinition;
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

    private final Pool<ClientLaser> laserPool = new Pool<>() {
        @Override
        protected ClientLaser newObject() {
            return new ClientLaser();
        }
    };
    private final Array<ClientLaser> lasers = new Array<>();

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

    public Array<ClientBullet> getBullets() {
        return bullets;
    }

    public Array<ClientLaser> getLasers() {
        return lasers;
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

        for (int i = lasers.size - 1; i >= 0; --i) {
            ClientLaser laser = lasers.get(i);

            if (!laser.update(dt)) {
                lasers.removeIndex(i);
                laserPool.free(laser);
            }
        }
    }

    public void end() {
        if (bullets.notEmpty()) {
            bulletPool.freeAll(bullets);
            bullets.removeRange(0, bullets.size - 1);
        }

        if (lasers.notEmpty()) {
            laserPool.freeAll(lasers);
            lasers.removeRange(0, lasers.size - 1);
        }
    }

    public void addBullet(float x, float y, float dirX, float dirY, BulletDefinition bullet, int index, @Nullable ClientCharacter character) {
        ClientBullet poolBullet = bulletPool.obtain();
        poolBullet.setDefinition(bullet);
        poolBullet.setStartPos(x, y);
        poolBullet.setPos(x, y);
        poolBullet.setAngleOffset(bullet.angleOffset());
        poolBullet.setSpeed(bullet.speed());
        poolBullet.setDirection(dirX, dirY);
        poolBullet.setLifetime(bullet.lifetime());
        poolBullet.setIndex(index);
        poolBullet.setOrbitPhase(MathUtils.PI2 * index / 5f);
        poolBullet.setAttached(character);
        bullets.add(poolBullet);
    }

    public void addLaser(float x, float y, float angleOffset, float width, float length, float lifetime, @Nullable ClientCharacter character) {
        ClientLaser poolLaser = laserPool.obtain();
        poolLaser.setPos(x, y);
        poolLaser.setAngleOffset(angleOffset);
        poolLaser.setAttached(character);
        poolLaser.setWidth(width);
        poolLaser.setLength(length);
        poolLaser.setLifetime(lifetime);
        lasers.add(poolLaser);
    }
}
