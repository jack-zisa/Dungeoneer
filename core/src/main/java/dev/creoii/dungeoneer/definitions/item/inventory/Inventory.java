package dev.creoii.dungeoneer.definitions.item.inventory;

import com.mojang.serialization.Codec;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.item.Item;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class Inventory implements Collection<Slot> {
    public static final Codec<Inventory> DB_CODEC = Codec.LONG.listOf().xmap(longs -> {
        Inventory inventory = new Inventory(longs.size());
        for (int i = 0; i < longs.size(); ++i) {
            long id = longs.get(i);
            if (id == -1) continue;
            Item item = DataManager.getItem(id);
            inventory.setItem(i, item);
        }
        return inventory;
    }, inventory -> {
        List<Long> items = new ArrayList<>();
        inventory.forEach(slot -> {
            if (slot.isEmpty()) {
                items.add(-1L);
            } else items.add(DataManager.getInternalId(DataManager.SchemaType.ITEM, slot.getItem().id()));
        });
        return items;
    });
    private final Slot[] slots;

    public Inventory(int size) {
        slots = new Slot[size];
        for (int i = 0; i < size; ++i) {
            slots[i] = new Slot(i);
        }
    }

    public Slot[] getSlots() {
        return slots;
    }

    @Nullable
    public Slot getSlot(int index) {
        if (index < 0 || index >= slots.length)
            return null;
        return slots[index];
    }

    public boolean setItem(int index, Item item, int count) {
        Slot slot = getSlot(index);
        if (slot != null) {
            return slot.setItem(item, count);
        }
        return false;
    }

    public boolean setItem(int index, Item item) {
        return setItem(index, item, 1);
    }

    public boolean swap(int index1, int index2) {
        Slot slot1 = getSlot(index1);
        Slot slot2 = getSlot(index2);

        if (slot1 == null || slot2 == null) return false;
        if (slot1 == slot2) return true;

        Item item1 = slot1.getItem();
        int count1 = slot1.getCount();

        Item item2 = slot2.getItem();
        int count2 = slot2.getCount();

        slot1.clear();
        slot2.clear();

        if (item2 != null) slot1.setItem(item2, count2);
        if (item1 != null) slot2.setItem(item1, count1);
        return true;
    }

    public int indexOf(Item item) {
        for (int i = 0; i < slots.length; i++) {
            Slot slot = slots[i];
            if (!slot.isEmpty() && slot.getItem().id().equals(item.id())) return i;
        }
        return -1;
    }

    @Nullable
    public Slot getNextAvailableSlot() {
        for (Slot slot : slots) {
            if (slot.isEmpty()) return slot;
        }
        return null;
    }

    @Nullable
    public Slot getNextAvailableSlot(Item item) {
        for (Slot slot : slots) {
            if (slot.isEmpty() && slot.isValid(item)) return slot;
        }
        return null;
    }

    public boolean addItem(Item item) {
        Slot slot = getNextAvailableSlot(item);
        if (slot == null) return false;
        return slot.setItem(item, 1);
    }

    @Override
    public boolean isEmpty() {
        for (Slot slot : slots) {
            if (!slot.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public boolean contains(Object o) {
        for (Slot slot : slots) {
            if (slot.equals(o)) return true;
        }
        return false;
    }

    public int size() {
        return slots.length;
    }

    @Override
    public Iterator<Slot> iterator() {
        return Arrays.asList(slots).iterator();
    }

    @Override
    public Object[] toArray() {
        return slots;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return Arrays.asList(slots).toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object object : c) {
            if (!contains(object)) return false;
        }
        return true;
    }

    @Override
    public boolean add(Slot slot) {
        throw new UnsupportedOperationException("Cannot add Slot to fixed-length Inventory.");
    }

    @Override
    public boolean addAll(Collection<? extends Slot> c) {
        throw new UnsupportedOperationException("Cannot add Slots to fixed-length Inventory.");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Cannot remove Slot from fixed-length Inventory.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Cannot remove Slots from fixed-length Inventory.");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Cannot remove Slots from fixed-length Inventory.");
    }

    @Override
    public void clear() {
        for (Slot slot : slots) {
            slot.clear();
        }
    }

    public List<Slot> clearAndGet() {
        List<Slot> slots = new ArrayList<>();
        forEach(slot -> {
            if (!slot.isEmpty()) {
                slot.clear();
                slots.add(slot);
            }
        });
        return slots;
    }
}
