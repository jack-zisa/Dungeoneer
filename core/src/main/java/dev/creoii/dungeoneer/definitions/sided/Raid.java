package dev.creoii.dungeoneer.definitions.sided;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.RaidDefinition;
import dev.creoii.dungeoneer.definitions.attack.bullet.*;
import dev.creoii.dungeoneer.definitions.attack.bullet.path.OrbitBulletPathType;
import dev.creoii.dungeoneer.util.Constants;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Iterator;

public abstract class Raid<B extends Bullet, BG extends BulletGroup, C extends Character, D extends DungeonMap> {
    private RaidDefinition raid;
    private Status status;
    private final D dungeonMap;
    private final Long2ObjectArrayMap<C> characters;
    private long endTime;

    private final Int2ObjectArrayMap<B> bullets = new Int2ObjectArrayMap<>();
    private final Int2ObjectArrayMap<BG> bulletGroups = new Int2ObjectArrayMap<>();

    private int nextBulletId = 0;

    public Raid(RaidDefinition raid, D dungeonMap) {
        this.raid = raid;
        this.dungeonMap = dungeonMap;
        status = Status.WAITING;
        characters = new Long2ObjectArrayMap<>();
    }

    public RaidDefinition get() {
        return raid;
    }

    public void set(@Nullable RaidDefinition raid) {
        this.raid = raid;

        if (raid != null) {
            endTime = System.currentTimeMillis() + Constants.RAID_DURATION_MS;
        } else endTime = -1L;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public D getDungeonMap() {
        return dungeonMap;
    }

    public Long2ObjectArrayMap<C> getCharacters() {
        return characters;
    }

    public void addCharacter(long accountId, C character) {
        characters.put(accountId, character);
    }

    public abstract Pool<B> getBulletPool();

    public abstract Pool<BG> getBulletGroupPool();

    public Int2ObjectArrayMap<B> getBullets() {
        return bullets;
    }

    public Int2ObjectArrayMap<BG> getBulletGroups() {
        return bulletGroups;
    }

    public boolean isNull() {
        return raid == null;
    }

    public long getRemainingTimeMs() {
        return Math.max(0, endTime - System.currentTimeMillis());
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void updateSpawnPositions(float spawnX, float spawnY) {
        characters.values().forEach(c -> c.setPos(spawnX, spawnY));
    }

    public void update(float dt) {
        Duration duration = Duration.ofMillis(getRemainingTimeMs()); // TODO: Remove as this is just testing
        /*if (duration.toSecondsPart() % 2 == 0) {
            Vector2 spawnPos = dungeonMap.getTemplate().spawnPos();
            addBullet(spawnPos.x * 8f, spawnPos.y * 8f, 1f, 0f, DataManager.getBullet("fire_shot"), 0, true);
        }*/

        Iterator<Int2ObjectMap.Entry<B>> bulletIterator = bullets.int2ObjectEntrySet().iterator();
        while (bulletIterator.hasNext()) {
            Int2ObjectMap.Entry<B> entry = bulletIterator.next();
            B bullet = entry.getValue();

            if (!bullet.update(dt)) {
                bulletIterator.remove();
                getBulletPool().free(bullet);
            } else bullet.applyTransform(dungeonMap, bullet.getStartX(), bullet.getStartY(), bullet.getDirX(), bullet.getDirY());
        }

        Iterator<Int2ObjectMap.Entry<BG>> groupIterator = bulletGroups.int2ObjectEntrySet().iterator();
        while (groupIterator.hasNext()) {
            Int2ObjectMap.Entry<BG> entry = groupIterator.next();
            BG bulletGroup = entry.getValue();

            if (!bulletGroup.update(dt)) {
                groupIterator.remove();
                getBulletGroupPool().free(bulletGroup);
            } else bulletGroup.applyTransform(dungeonMap, bulletGroup.getStartX(), bulletGroup.getStartY(), bulletGroup.getDirX(), bulletGroup.getDirY());
        }
    }

    public void end() {
        if (!bullets.isEmpty()) {
            bullets.values().forEach(b -> getBulletPool().free(b));
            bullets.clear();
        }

        if (!bulletGroups.isEmpty()) {
            bulletGroups.values().forEach(bg -> getBulletGroupPool().free(bg));
            bulletGroups.clear();
        }

        status = Status.WAITING;
        characters.clear();
        nextBulletId = 0;
        endTime = -1L;
    }

    @SuppressWarnings("unchecked")
    public void addBullet(float x, float y, float dirX, float dirY, BulletType bullet, int index, boolean enemy) {
        BulletNode<?> poolBullet = createHierarchy(x, y, dirX, dirY, bullet, index, enemy, 1);
        if (bullet instanceof SingleBulletType) {
            bullets.put(nextBulletId++, (B) poolBullet);
        } else bulletGroups.put(nextBulletId++, (BG) poolBullet);
    }

    private BulletNode<?> createHierarchy(float x, float y, float dirX, float dirY, BulletType bullet, int index, boolean enemy, int siblings) {
        BulletNode<?> node = createBullet(x, y, dirX, dirY, bullet, index, enemy, siblings);
        if (node instanceof BulletGroup group) {
            GroupBulletType def = (GroupBulletType) bullet;
            int nodeSiblings = def.children().size();
            for (int i = 0; i < nodeSiblings; i++) {
                GroupBulletType.Child childDef = def.children().get(i);
                BulletNode<?> child = createHierarchy(x, y, dirX, dirY, childDef.definition(), i, enemy, nodeSiblings);
                child.setOffset(childDef.offset().x, childDef.offset().y);
                child.setParent(group);
                group.addChild(child);
            }
        }
        return node;
    }

    @SuppressWarnings("unchecked")
    public BulletNode<?> createBullet(float x, float y, float dirX, float dirY, BulletType bullet, int index, boolean enemy, int siblings) {
        BulletNode<?> poolBullet;
        if (bullet instanceof SingleBulletType singleBulletType) {
            poolBullet = getBulletPool().obtain();
            ((Bullet) poolBullet).setEnemy(enemy);
            ((BulletNode<SingleBulletType>) poolBullet).setType(singleBulletType);
        } else {
            poolBullet = getBulletGroupPool().obtain();
            ((BulletNode<GroupBulletType>) poolBullet).setType((GroupBulletType) bullet);
        }
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

    public enum Status {
        WAITING,
        ACTIVE,
        END
    }
}
