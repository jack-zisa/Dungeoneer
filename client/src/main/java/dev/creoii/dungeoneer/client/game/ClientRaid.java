package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletType;
import dev.creoii.dungeoneer.definitions.Raid;
import dev.creoii.dungeoneer.definitions.attack.bullet.GroupBulletType;
import dev.creoii.dungeoneer.definitions.attack.bullet.SingleBulletType;
import dev.creoii.dungeoneer.definitions.attack.bullet.path.OrbitBulletPathType;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;
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

    private final Pool<ClientBulletGroup> bulletGroupPool = new Pool<>() {
        @Override
        protected ClientBulletGroup newObject() {
            return new ClientBulletGroup();
        }
    };
    private final Array<ClientBulletGroup> bulletGroups = new Array<>();

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

    public Array<ClientBulletGroup> getBulletGroups() {
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
            ClientBullet bullet = bullets.get(i);
            if (!bullet.update(dt)) {
                bullets.removeIndex(i);
                bulletPool.free(bullet);
            }
        }

        for (int i = bulletGroups.size - 1; i >= 0; --i) {
            ClientBulletGroup bulletGroup = bulletGroups.get(i);
            if (!bulletGroup.update(dt)) {
                bulletGroups.removeIndex(i);
                bulletGroupPool.free(bulletGroup);
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

    public void addBullet(float x, float y, float dirX, float dirY, BulletType bullet, int index, @Nullable ClientCharacter shooter) {
        BulletNode poolBullet = createHierarchy(x, y, dirX, dirY, bullet, index);

        if (bullet instanceof SingleBulletType) {
            ClientBullet clientBullet = (ClientBullet) poolBullet;
            bullets.add(clientBullet);
        } else {
            ClientBulletGroup clientBulletGroup = (ClientBulletGroup) poolBullet;
            GroupBulletType groupBulletDefinition = (GroupBulletType) bullet;
            for (int i = 0; i < groupBulletDefinition.children().size(); ++i) {
                GroupBulletType.Child child1 = groupBulletDefinition.children().get(i);
                BulletNode childBullet = createHierarchy(x, y, dirX, dirY, child1.definition(), i);
                childBullet.setOffset(child1.offset().x, child1.offset().y);
                childBullet.setParent(poolBullet);
                clientBulletGroup.addChild(childBullet);
            }
            bulletGroups.add(clientBulletGroup);
        }
    }

    public BulletNode createBullet(float x, float y, float dirX, float dirY, BulletType bullet, int index) {
        BulletNode poolBullet = bullet instanceof SingleBulletType ? bulletPool.obtain() : bulletGroupPool.obtain();
        poolBullet.setType(bullet);
        poolBullet.setStartPos(x, y);
        poolBullet.setPos(x, y);
        poolBullet.setSpeed(bullet.speed());
        poolBullet.setDirection(dirX, dirY);
        poolBullet.setLifetime(bullet.lifetime());
        poolBullet.setIndex(index);

        if (poolBullet.getPath() instanceof OrbitBulletPathType.OrbitBulletPathInstance instance) {
            instance.setOrbitPhase(MathUtils.PI2 * index / 5f);
        }
        return poolBullet;
    }

    private BulletNode createHierarchy(float x, float y, float dirX, float dirY, BulletType bullet, int index) {
        BulletNode node = createBullet(x, y, dirX, dirY, bullet, index);
        if (node instanceof ClientBulletGroup group) {
            GroupBulletType def = (GroupBulletType) bullet;
            for (int i = 0; i < def.children().size(); i++) {
                GroupBulletType.Child childDef = def.children().get(i);
                BulletNode child = createHierarchy(x, y, dirX, dirY, childDef.definition(), i);
                child.setOffset(childDef.offset().x, childDef.offset().y);
                child.setParent(node);
                group.addChild(child);
            }
        }
        return node;
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
