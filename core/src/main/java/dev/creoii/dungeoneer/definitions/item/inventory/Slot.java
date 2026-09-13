package dev.creoii.dungeoneer.definitions.item.inventory;

import dev.creoii.dungeoneer.definitions.item.EquipmentItem;
import dev.creoii.dungeoneer.definitions.item.Item;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class Slot {
    private final int index;
    private @Nullable Item item;
    private int count;
    private EquipmentItem.EquipmentType slotType;

    public Slot(int index, @Nullable Item item, int count) {
        this.index = index;
        setItem(item, count);
    }

    public Slot(int index, @Nullable Item item) {
        this(index, item, 1);
    }

    public Slot(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public @Nullable Item getItem() {
        return item;
    }

    public boolean setItem(@Nullable Item item) {
        return setItem(item, 1);
    }

    public boolean setItem(@Nullable Item item, int count) {
        if (item == null || count <= 0) {
            boolean isEmpty = isEmpty();
            clear();
            return !isEmpty; // If this slot was already empty, we did not successfully set it empty
        }

        if (!isValid(item)) {
            return false;
        }

        this.item = item;

        if (item.stackable()) {
            this.count = count;
        } else this.count = 1;

        return true;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        if (count == 0) item = null;
        this.count = count;
    }

    public void clear() {
        item = null;
        count = 0;
    }

    public boolean isEmpty() {
        return item == null || count <= 0;
    }

    public EquipmentItem.EquipmentType getSlotType() {
        return slotType;
    }

    public void setSlotType(EquipmentItem.EquipmentType slotType) {
        this.slotType = slotType;
    }

    public boolean isValid(Item item) {
        return isEmpty() && (slotType == null || item.type() == slotType.getType());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Slot slot = (Slot) o;
        return index == slot.index && count == slot.count && Objects.equals(item, slot.item) && slotType == slot.slotType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, item, count, slotType);
    }
}
