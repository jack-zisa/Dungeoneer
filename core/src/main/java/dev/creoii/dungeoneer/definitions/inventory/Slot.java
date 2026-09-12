package dev.creoii.dungeoneer.definitions.inventory;

import dev.creoii.dungeoneer.definitions.item.EquipmentItem;
import dev.creoii.dungeoneer.definitions.item.Item;
import org.jspecify.annotations.Nullable;

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
        if (item.type() != slotType.getType())
            return false;

        this.item = item;
        if (item == null) count = 0;

        return true;
    }

    public boolean setItem(@Nullable Item item, int count) {
        if (item == null || count <= 0) {
            this.item = null;
            this.count = 0;
            return false;
        }

        if (item.type() != slotType.getType())
            return false;

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

    public boolean isEmpty() {
        return item != null && count > 0;
    }

    public EquipmentItem.EquipmentType getSlotType() {
        return slotType;
    }

    public void setSlotType(EquipmentItem.EquipmentType slotType) {
        this.slotType = slotType;
    }
}
