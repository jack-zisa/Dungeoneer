package dev.creoii.dungeoneer.client.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.screen.game.GameScreen;
import dev.creoii.dungeoneer.client.screen.main.MainScreen;
import dev.creoii.dungeoneer.definitions.Raid;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.definitions.attack.bullet.*;
import dev.creoii.dungeoneer.definitions.attack.bullet.path.OrbitBulletPathType;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

public class ClientRaid {
    private final Dungeoneer client;
    private Raid raid;
    private final ClientDungeonMap dungeon;
    private long endTime;

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

    public ClientRaid(Dungeoneer client, Raid raid) {
        this.client = client;
        this.raid = raid;
        dungeon = new ClientDungeonMap();
    }

    public Raid get() {
        return raid;
    }

    public void set(@Nullable Raid raid) {
        this.raid = raid;

        if (raid != null) {
            endTime = System.currentTimeMillis() + Constants.RAID_DURATION_MS;
        } else endTime = -1L;
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

    public long getRemainingTimeMs() {
        return Math.max(0, endTime - System.currentTimeMillis());
    }

    public String getRemainingTimeString() {
        Duration duration = Duration.ofMillis(getRemainingTimeMs());
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        if (hours <= 0L && minutes < 0L) {
            return String.valueOf(seconds);
        } else if (hours <= 0L) {
            return String.format("%02d:%02d", minutes, seconds);
        } else return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void syncTimer(long timeRemaining) {
        endTime = System.currentTimeMillis() + timeRemaining;
    }

    public void update(float dt) {
        if (client.getScreen() instanceof GameScreen gameScreen) {
            long remaining = getRemainingTimeMs();
            if (remaining <= 0L) {
                Gdx.app.postRunnable(() -> {
                    client.getState().setStatus(ClientState.Status.LOBBY);
                    client.getState().getCurrentRaid().end();
                    client.setScreen(new MainScreen(client));
                });
            } else gameScreen.getTimeRemainingLabel().setText(getRemainingTimeString());
        }

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

    public void end() {
        if (bullets.notEmpty()) {
            bulletPool.freeAll(bullets);
            bullets.clear();
        }

        if (bulletGroups.notEmpty()) {
            bulletGroupPool.freeAll(bulletGroups);
            bulletGroups.clear();
        }

        if (lasers.notEmpty()) {
            laserPool.freeAll(lasers);
            lasers.clear();
        }
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
        poolBullet.setStartDirection(dirX, dirY);
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
