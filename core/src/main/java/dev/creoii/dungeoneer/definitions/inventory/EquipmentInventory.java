package dev.creoii.dungeoneer.definitions.inventory;

import dev.creoii.dungeoneer.definitions.item.EquipmentItem;

public class EquipmentInventory extends Inventory {
    public EquipmentInventory(EquipmentItem.EquipmentType weapon, EquipmentItem.EquipmentType ability, EquipmentItem.EquipmentType armor, EquipmentItem.EquipmentType accessory) {
        super(4);
        getSlot(0).setSlotType(weapon);
        getSlot(1).setSlotType(ability);
        getSlot(2).setSlotType(armor);
        getSlot(3).setSlotType(accessory);
    }

    public Slot getWeaponSlot() {
        return getSlot(0);
    }

    public Slot getAbilitySlot() {
        return getSlot(1);
    }

    public Slot getArmorSlot() {
        return getSlot(2);
    }

    public Slot getAccessorySlot() {
        return getSlot(3);
    }
}
