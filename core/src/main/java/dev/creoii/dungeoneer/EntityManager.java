package dev.creoii.dungeoneer;

import dev.creoii.dungeoneer.definitions.attack.bullet.BulletGroup;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;
import dev.creoii.dungeoneer.definitions.sided.Entity;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.network.s2c.raid.AddEntitiesS2C;
import dev.creoii.dungeoneer.util.EntityOwnable;
import dev.creoii.dungeoneer.util.Tickable;

import java.util.*;

public class EntityManager<R extends Raid<?, ?, ?, ?>, E extends Entity<R>> implements Tickable {
    private final R raid;
    private final Set<E> entities;
    private final Map<Long, E> idToObj;
    private final PriorityQueue<Long> freeIds;
    private long nextId;

    public EntityManager(R raid, int capacity) {
        this.raid = raid;
        this.entities = new HashSet<>(capacity);
        idToObj = new HashMap<>(capacity);
        freeIds = new PriorityQueue<>(capacity);
        nextId = Entity.CHARACTER_ID + 1L; // nextId should never return 0L
    }

    public R getRaid() {
        return raid;
    }

    public Set<E> getEntities() {
        return entities;
    }

    public Map<Long, E> getIdToObj() {
        return idToObj;
    }

    public PriorityQueue<Long> getFreeIds() {
        return freeIds;
    }

    public long getNextId() {
        return nextId;
    }

    public long getAndIncrementNextId() {
        return nextId++;
    }

    public void setNextId(long nextId) {
        this.nextId = nextId;
    }

    @SuppressWarnings("unchecked")
    public boolean addRecursive(E entity) {
        if (!add(entity)) return false;
        if (entity instanceof BulletGroup<?> bulletGroup) {
            bulletGroup.getChildren().forEach(bulletNode -> {
                addRecursive((E) bulletNode);
            });
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public int addRecursive(E entity, List<AddEntitiesS2C.Entry> entries, int index) {
        AddEntitiesS2C.Entry entry = entries.get(index);
        if (!add(entity, entry.entityId()))
            return -1;

        index++;
        if (entity instanceof BulletGroup<?> group) {
            for (BulletNode<?, ?> child : group.getChildren()) {
                index = addRecursive((E) child, entries, index);
                if (index == -1)
                    return -1;
            }
        }
        return index;
    }

    public boolean add(E entity) {
        if (entity.id() == Entity.CHARACTER_ID)
            return false;

        if (entity.id() == -1L) {
            long id = freeIds.isEmpty() ? nextId++ : freeIds.poll();
            entity.setId(id);
        }

        idToObj.put(entity.id(), entity);
        return entities.add(entity);
    }

    public boolean add(E entity, long entityId) {
        if (entityId == Entity.CHARACTER_ID) {
            return false;
        }
        entity.setId(entityId);
        idToObj.put(entityId, entity);
        return entities.add(entity);
    }

    public boolean remove(E entity) {
        if (!entities.remove(entity)) {
            return false;
        }
        idToObj.remove(entity.id());
        freeIds.offer(entity.id());
        return true;
    }

    public boolean remove(long id) {
        E removed = idToObj.remove(id);
        if (removed == null) {
            return false;
        }
        entities.remove(removed);
        freeIds.offer(id);
        return true;
    }

    public boolean contains(E entity) {
        return idToObj.containsKey(entity.id());
    }

    public boolean contains(long id) {
        return idToObj.containsKey(id);
    }

    public E get(long id) {
        return idToObj.get(id);
    }

    public Collection<E> getValues() {
        return idToObj.values();
    }

    @Override
    public boolean tick(float dt) {
        Iterator<E> entityIterator = entities.iterator();
        while (entityIterator.hasNext()) {
            E entity = entityIterator.next();

            if (entity instanceof EntityOwnable ownable && ownable.getOwner() != null) // Entity owners should tick their children
                continue;

            if (!entity.tick(dt)) {
                entityIterator.remove();
                entity.die();
                raid.free(entity);
                remove(entity.id());
            }
        }

        return true;
    }
}
