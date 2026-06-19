package dev.creoii.dungeoneer.definitions.sided;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;
import dev.creoii.dungeoneer.definitions.RaidDefinition;
import dev.creoii.dungeoneer.definitions.attack.bullet.*;
import dev.creoii.dungeoneer.definitions.attack.bullet.path.OrbitBulletPathType;
import dev.creoii.dungeoneer.util.Constants;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;

public abstract class Raid<B extends Bullet, BG extends BulletGroup> {
    private RaidDefinition raid;
    private long endTime;

    private final Int2ObjectArrayMap<B> bullets = new Int2ObjectArrayMap<>();
    private final Int2ObjectArrayMap<BG> bulletGroups = new Int2ObjectArrayMap<>();

    private int nextBulletId = 0;

    public Raid(RaidDefinition raid) {
        this.raid = raid;
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

    public void update(float dt) {
        Iterator<Int2ObjectMap.Entry<B>> bulletIterator = bullets.int2ObjectEntrySet().iterator();
        while (bulletIterator.hasNext()) {
            Int2ObjectMap.Entry<B> entry = bulletIterator.next();
            B bullet = entry.getValue();

            if (!bullet.update(dt)) {
                bulletIterator.remove();
                getBulletPool().free(bullet);
            } else bullet.applyTransform(bullet.getStartX(), bullet.getStartY(), bullet.getDirX(), bullet.getDirY());
        }

        Iterator<Int2ObjectMap.Entry<BG>> groupIterator = bulletGroups.int2ObjectEntrySet().iterator();
        while (groupIterator.hasNext()) {
            Int2ObjectMap.Entry<BG> entry = groupIterator.next();
            BG bulletGroup = entry.getValue();

            if (!bulletGroup.update(dt)) {
                groupIterator.remove();
                getBulletGroupPool().free(bulletGroup);
            } else bulletGroup.applyTransform(bulletGroup.getStartX(), bulletGroup.getStartY(), bulletGroup.getDirX(), bulletGroup.getDirY());
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
    }

    @SuppressWarnings("unchecked")
    public void addBullet(float x, float y, float dirX, float dirY, BulletType bullet, int index, @Nullable Character shooter) {
        BulletNode<?> poolBullet = createHierarchy(x, y, dirX, dirY, bullet, index, 1);
        if (bullet instanceof SingleBulletType) {
            bullets.put(nextBulletId++, (B) poolBullet);
        } else bulletGroups.put(nextBulletId++, (BG) poolBullet);
    }

    private BulletNode<?> createHierarchy(float x, float y, float dirX, float dirY, BulletType bullet, int index, int siblings) {
        BulletNode<?> node = createBullet(x, y, dirX, dirY, bullet, index, siblings);
        if (node instanceof BulletGroup group) {
            GroupBulletType def = (GroupBulletType) bullet;
            int nodeSiblings = def.children().size();
            for (int i = 0; i < nodeSiblings; i++) {
                GroupBulletType.Child childDef = def.children().get(i);
                BulletNode<?> child = createHierarchy(x, y, dirX, dirY, childDef.definition(), i, nodeSiblings);
                child.setOffset(childDef.offset().x, childDef.offset().y);
                child.setParent(group);
                group.addChild(child);
            }
        }
        return node;
    }

    @SuppressWarnings("unchecked")
    public BulletNode<?> createBullet(float x, float y, float dirX, float dirY, BulletType bullet, int index, int siblings) {
        BulletNode<?> poolBullet;
        if (bullet instanceof SingleBulletType singleBulletType) {
            poolBullet = getBulletPool().obtain();
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
}
