package dev.creoii.dungeoneer;

import dev.creoii.dungeoneer.definitions.sided.Entity;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.util.Tickable;

import java.util.*;

public class EntityManager<R extends Raid<?, ?, ?, ?>> implements Tickable {
    private final R raid;
    private final Set<Entity<R>> entities;
    private final Map<Long, Entity<R>> idToObj;
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

    public Set<Entity<R>> getEntities() {
        return entities;
    }

    public Map<Long, Entity<R>> getIdToObj() {
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

    public boolean add(Entity<R> entity) {
        if (entity.id() == Entity.CHARACTER_ID)
            return false;
        long id = freeIds.isEmpty() ? nextId++ : freeIds.poll();
        entity.setId(id);
        idToObj.put(id, entity);
        return entities.add(entity);
    }

    public boolean add(Entity<R> entity, long entityId) {
        if (entityId == Entity.CHARACTER_ID)
            return false;
        if (idToObj.containsKey(entityId)) return add(entity);
        entity.setId(entityId);
        idToObj.put(entityId, entity);
        return entities.add(entity);
    }

    public boolean remove(Entity<R> entity) {
        if (!entities.remove(entity)) {
            return false;
        }
        idToObj.remove(entity.id());
        freeIds.offer(entity.id());
        return true;
    }

    public boolean remove(long id) {
        Entity<R> removed = idToObj.remove(id);
        if (removed == null) {
            return false;
        }
        entities.remove(removed);
        freeIds.offer(id);
        return true;
    }

    public boolean contains(Entity<R> entity) {
        return idToObj.containsKey(entity.id());
    }

    public boolean contains(long id) {
        return idToObj.containsKey(id);
    }

    public Entity<R> get(long id) {
        return idToObj.get(id);
    }

    public Collection<Entity<R>> getValues() {
        return idToObj.values();
    }

    @Override
    public void tick(float dt) {
    }
}
