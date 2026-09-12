package dev.creoii.dungeoneer.definitions.item.inventory;

import dev.creoii.dungeoneer.definitions.CharacterClass;
import dev.creoii.dungeoneer.definitions.item.*;
import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.util.stat.ModifierEntry;
import dev.creoii.dungeoneer.util.stat.Stat;
import dev.creoii.dungeoneer.util.stat.StatContainer;

import java.util.UUID;

public class EquipmentInventory extends Inventory {
    private final Character<?> character;
    private final UUID[][] statModifiers;

    public EquipmentInventory(Character<?> character, EquipmentItem.EquipmentType weapon, EquipmentItem.EquipmentType ability, EquipmentItem.EquipmentType armor, EquipmentItem.EquipmentType accessory) {
        super(4);
        if (weapon.getType() != Item.Type.WEAPON || ability.getType() != Item.Type.ABILITY || armor.getType() != Item.Type.ARMOR || accessory.getType() != Item.Type.ACCESSORY)
            throw new IllegalArgumentException("Attempted to create Equipment Inventory with inapplicable equipment types!");

        this.character = character;
        statModifiers = new UUID[4][Stat.Type.values().length];

        getSlot(0).setSlotType(weapon);
        getSlot(1).setSlotType(ability);
        getSlot(2).setSlotType(armor);
        getSlot(3).setSlotType(accessory);
    }

    public EquipmentInventory(Character<?> character, CharacterClass.ClassEquipment equipment) {
        this(character, equipment.weapon(), equipment.ability(), equipment.armor(), equipment.accessory());
    }

    @Override
    public boolean setItem(int index, Item item, int count) {
        if (!super.setItem(index, item, count))
            return false;
        removeModifiers(index);
        if (item instanceof EquipmentItem equipmentItem) {
            applyModifiers(index, equipmentItem.statBonus());
        }
        return true;
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

    public WeaponItem getWeapon() {
        return (WeaponItem) getWeaponSlot().getItem();
    }

    public AbilityItem getAbility() {
        return (AbilityItem) getAbilitySlot().getItem();
    }

    public ArmorItem getArmor() {
        return (ArmorItem) getArmorSlot().getItem();
    }

    public AccessoryItem getAccessory() {
        return (AccessoryItem) getAccessorySlot().getItem();
    }

    private void removeModifiers(int index) {
        UUID[] uuids = statModifiers[index];
        for (Stat.Type type : Stat.Type.values()) {
            UUID uuid = uuids[type.ordinal()];
            if (uuid != null) {
                character.getStats().removeModifier(type, uuid);
                uuids[type.ordinal()] = null;
            }
        }
    }

    private void applyModifiers(int index, StatContainer statBonus) {
        UUID[] uuids = statModifiers[index];
        applyModifier(Stat.Type.HEALTH, uuids, statBonus.health().value());
        applyModifier(Stat.Type.DEFENSE, uuids, statBonus.defense().value());
        applyModifier(Stat.Type.SPEED, uuids, statBonus.speed().value());
        applyModifier(Stat.Type.DEXTERITY, uuids, statBonus.dexterity().value());
        applyModifier(Stat.Type.VITALITY, uuids, statBonus.vitality().value());
    }

    public void applyModifier(Stat.Type type, UUID[] uuids, float amount) {
        if (amount == 0)
            return;
        UUID uuid = UUID.randomUUID();
        uuids[type.ordinal()] = uuid;
        character.getStats().applyModifier(new ModifierEntry(type, uuid, amount, ModifierEntry.Operation.ADD, ModifierEntry.ModifierType.BASE));
    }
}
