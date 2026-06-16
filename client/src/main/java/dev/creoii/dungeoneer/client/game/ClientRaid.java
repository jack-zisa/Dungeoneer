package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.attack.bullet.*;
import dev.creoii.dungeoneer.definitions.Raid;
import dev.creoii.dungeoneer.definitions.attack.bullet.path.OrbitBulletPathType;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;
import org.jspecify.annotations.Nullable;

public class ClientRaid {
    private Raid raid;
    private final ClientDungeonMap dungeon;

    private final Pool<Bullet> bulletPool = new Pool<>() {
        @Override
        protected Bullet newObject() {
            return new Bullet();
        }
    };
    private final Array<Bullet> bullets = new Array<>();

    private final Pool<BulletGroup> bulletGroupPool = new Pool<>() {
        @Override
        protected BulletGroup newObject() {
            return new BulletGroup();
        }
    };
    private final Array<BulletGroup> bulletGroups = new Array<>();

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

    public Array<Bullet> getBullets() {
        return bullets;
    }

    public Array<BulletGroup> getBulletGroups() {
        return bulletGroups;
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
            Bullet bullet = bullets.get(i);
            if (!bullet.update(dt)) {
                bullets.removeIndex(i);
                bulletPool.free(bullet);
            } else bullet.applyTransform(bullet.getStartX(), bullet.getStartY(), bullet.getDirX(), bullet.getDirY());
        }

        for (int i = bulletGroups.size - 1; i >= 0; --i) {
            BulletGroup bulletGroup = bulletGroups.get(i);
            if (!bulletGroup.update(dt)) {
                bulletGroups.removeIndex(i);
                bulletGroupPool.free(bulletGroup);
            } else bulletGroup.applyTransform(bulletGroup.getStartX(), bulletGroup.getStartY(), bulletGroup.getDirX(), bulletGroup.getDirY());
        }

        for (int i = lasers.size - 1; i >= 0; --i) {
            ClientLaser laser = lasers.get(i);
            if (!laser.update(dt)) {
                lasers.removeIndex(i);
                laserPool.free(laser);
            }
        }
    }

    public void end() { // TODO: Fix bullets preserved across raids
        if (bullets.notEmpty()) {
            bullets.removeRange(0, bullets.size - 1);
        }
        bulletPool.freeAll(bullets);

        if (lasers.notEmpty()) {
            lasers.removeRange(0, lasers.size - 1);
        }
        laserPool.freeAll(lasers);
    }

    public void addBullet(float x, float y, float dirX, float dirY, BulletType bullet, int index, @Nullable ClientCharacter shooter) {
        BulletNode poolBullet = createHierarchy(x, y, dirX, dirY, bullet, index, 1);
        if (bullet instanceof SingleBulletType) {
            bullets.add((Bullet) poolBullet);
        } else bulletGroups.add((BulletGroup) poolBullet);
    }

    private BulletNode createHierarchy(float x, float y, float dirX, float dirY, BulletType bullet, int index, int siblings) {
        BulletNode node = createBullet(x, y, dirX, dirY, bullet, index, siblings);
        if (node instanceof BulletGroup group) {
            GroupBulletType def = (GroupBulletType) bullet;
            int nodeSiblings = def.children().size();
            for (int i = 0; i < nodeSiblings; i++) {
                GroupBulletType.Child childDef = def.children().get(i);
                BulletNode child = createHierarchy(x, y, dirX, dirY, childDef.definition(), i, nodeSiblings);
                child.setOffset(childDef.offset().x, childDef.offset().y);
                child.setParent(group);
                group.addChild(child);
            }
        }
        return node;
    }

    public BulletNode createBullet(float x, float y, float dirX, float dirY, BulletType bullet, int index, int siblings) {
        BulletNode poolBullet = bullet instanceof SingleBulletType ? bulletPool.obtain() : bulletGroupPool.obtain();
        poolBullet.setType(bullet);
        poolBullet.setStartPos(x, y);
        poolBullet.setDirection(dirX, dirY);
        poolBullet.setSpeed(bullet.speed());
        poolBullet.setLifetime(bullet.lifetime());
        poolBullet.setIndex(index);
        if (poolBullet.getPath() instanceof OrbitBulletPathType.OrbitBulletPathInstance instance) {
            float phase = siblings <= 1 ? 0f : MathUtils.PI2 * index / siblings;
            instance.setOrbitPhase(phase);
        }
        return poolBullet;
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
