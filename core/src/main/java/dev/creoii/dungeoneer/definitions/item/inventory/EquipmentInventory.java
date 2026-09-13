package dev.creoii.dungeoneer.definitions.item.inventory;

import dev.creoii.dungeoneer.definitions.CharacterClass;
import dev.creoii.dungeoneer.definitions.item.*;
import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.util.stat.ModifierEntry;
import dev.creoii.dungeoneer.util.stat.Stat;
import dev.creoii.dungeoneer.util.stat.StatContainer;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class EquipmentInventory extends Inventory {
    private final Character<?> character;

    public EquipmentInventory(Character<?> character, EquipmentItem.@Nullable EquipmentType weapon, EquipmentItem.@Nullable EquipmentType ability, EquipmentItem.@Nullable EquipmentType armor, EquipmentItem.@Nullable EquipmentType accessory) {
        super(4);
        if ((weapon != null && weapon.getType() != Item.Type.WEAPON) || (ability != null && ability.getType() != Item.Type.ABILITY) || (armor != null && armor.getType() != Item.Type.ARMOR) || (accessory != null && accessory.getType() != Item.Type.ACCESSORY))
            throw new IllegalArgumentException("Attempted to create Equipment Inventory with inapplicable equipment types!");

        this.character = character;

        getSlot(0).setSlotType(weapon);
        getSlot(1).setSlotType(ability);
        getSlot(2).setSlotType(armor);
        getSlot(3).setSlotType(accessory);
    }

    public EquipmentInventory(Character<?> character, CharacterClass.ClassEquipment equipment, Inventory savedEquipment) {
        this(character, equipment.weapon(), equipment.ability(), equipment.armor(), equipment.accessory());
        savedEquipment.forEach(slot -> setItem(slot.getIndex(), slot.getItem(), slot.getCount()));
    }

    public static EquipmentInventory createEmpty(Character<?> character) {
        return new EquipmentInventory(character, null, null, null, null);
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

    /**
     * Generates a unique deterministic UUID for each character/slot/stat combination.
     */
    private UUID getModifierUuid(int index, Stat.Type type) {
        return UUID.nameUUIDFromBytes((character.get().id() + index + type.name()).getBytes(StandardCharsets.UTF_8));
    }

    private void removeModifiers(int index) {
        for (Stat.Type type : Stat.Type.values()) {
            character.getStats().removeModifier(type, getModifierUuid(index, type));
        }
    }

    private void applyModifiers(int index, StatContainer statBonus) {
        applyModifier(index, Stat.Type.HEALTH, statBonus.health().value());
        applyModifier(index, Stat.Type.DEFENSE, statBonus.defense().value());
        applyModifier(index, Stat.Type.SPEED, statBonus.speed().value());
        applyModifier(index, Stat.Type.DEXTERITY, statBonus.dexterity().value());
        applyModifier(index, Stat.Type.VITALITY, statBonus.vitality().value());
    }

    public void applyModifier(int index, Stat.Type type, float amount) {
        if (amount == 0)
            return;
        UUID uuid = getModifierUuid(index, type);
        character.getStats().applyModifier(new ModifierEntry(type, uuid, amount, ModifierEntry.Operation.ADD, ModifierEntry.ModifierType.BASE));
    }
}
