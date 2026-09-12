package dev.creoii.dungeoneer.definitions.inventory;

import dev.creoii.dungeoneer.definitions.CharacterClass;
import dev.creoii.dungeoneer.definitions.item.EquipmentItem;
import dev.creoii.dungeoneer.definitions.item.Item;

public class EquipmentInventory extends Inventory {
    public EquipmentInventory(EquipmentItem.EquipmentType weapon, EquipmentItem.EquipmentType ability, EquipmentItem.EquipmentType armor, EquipmentItem.EquipmentType accessory) {
        super(4);
        if (weapon.getType() != Item.Type.WEAPON || ability.getType() != Item.Type.ABILITY || armor.getType() != Item.Type.ARMOR || accessory.getType() != Item.Type.ACCESSORY)
            throw new IllegalArgumentException("Attempted to create Equipment Inventory with inapplicable equipment types!");

        getSlot(0).setSlotType(weapon);
        getSlot(1).setSlotType(ability);
        getSlot(2).setSlotType(armor);
        getSlot(3).setSlotType(accessory);
    }

    public EquipmentInventory(CharacterClass.ClassEquipment equipment) {
        this(equipment.weapon(), equipment.ability(), equipment.armor(), equipment.accessory());
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
