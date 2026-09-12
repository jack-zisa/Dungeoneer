package dev.creoii.dungeoneer.definitions.inventory;

import dev.creoii.dungeoneer.definitions.item.Item;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class Inventory {
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

    public void setItem(int index, Item item, int count) {
        Slot slot = getSlot(index);
        if (slot != null) {
            slot.setItem(item, count);
        }
    }

    public void setItem(int index, Item item) {
        setItem(index, item, 1);
    }

    public int getSize() {
        return slots.length;
    }

    public void forEach(Consumer<Slot> action) {
        for (Slot slot : slots) {
            action.accept(slot);
        }
    }
}
