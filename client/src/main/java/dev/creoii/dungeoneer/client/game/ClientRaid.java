package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
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

    public void addBullet(float x, float y, float dirX, float dirY, float angleOffset, float rotation, float acceleration, float speed, float lifetime, float amplitude, float frequency, float orbitRadius, float orbitSpeed, int index) {
        ClientBullet bullet = bulletPool.obtain();
        bullet.setStartPos(x, y);
        bullet.setPos(x, y);
        bullet.setAngleOffset(angleOffset);
        bullet.setRotation(rotation);
        bullet.setSpeed(speed);
        bullet.setDirection(dirX, dirY);
        bullet.setLifetime(lifetime);
        bullet.setAmplitude(amplitude);
        bullet.setFrequency(frequency);
        bullet.setIndex(index);
        bullet.setAcceleration(acceleration);
        bullet.setOrbitSpeed(MathUtils.PI2 * orbitSpeed);
        bullet.setOrbitRadius(orbitRadius);
        bullet.setOrbitPhase(MathUtils.PI2 * index / 5f);
        bullets.add(bullet);
    }
}
