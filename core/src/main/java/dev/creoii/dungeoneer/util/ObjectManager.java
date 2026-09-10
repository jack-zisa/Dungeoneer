package dev.creoii.dungeoneer.util;

import dev.creoii.dungeoneer.util.logging.Logger;

import java.util.*;

public abstract class ObjectManager<I, T extends Identifiable<I>> implements Identifiable<String>, Tickable {
    private final Logger logger;
    private final Set<T> objects;
    private final Map<I, T> idToObj;

    public ObjectManager(int capacity) {
        logger = new Logger("Manager/" + id());
        this.objects = new HashSet<>(capacity);
        idToObj = new HashMap<>(capacity);
    }

    public Logger getLogger() {
        return logger;
    }

    public boolean add(T obj) {
        idToObj.put(obj.id(), obj);
        return objects.add(obj);
    }

    public boolean remove(T obj) {
        idToObj.remove(obj.id());
        return objects.remove(obj);
    }

    public boolean remove(I id) {
        T removed = idToObj.remove(id);
        if (removed != null)
            return objects.remove(removed);
        return false;
    }

    public boolean contains(T obj) {
        return idToObj.containsKey(obj.id()) || objects.contains(obj);
    }

    public boolean contains(I id) {
        return idToObj.containsKey(id);
    }

    public T get(I id) {
        return idToObj.get(id);
    }

    public Collection<T> getValues() {
        return idToObj.values();
    }
}
