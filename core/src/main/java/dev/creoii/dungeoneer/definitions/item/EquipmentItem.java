package dev.creoii.dungeoneer.definitions.item;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.util.stat.StatContainer;

public sealed interface EquipmentItem extends Item permits AbilityItem, AccessoryItem, ArmorItem, WeaponItem {
    StatContainer statBonus();

    @Override
    default boolean equippable() {
        return true;
    }

    @Override
    default boolean stackable() {
        return false;
    }

    static <T extends EquipmentItem> RecordCodecBuilder<T, StatContainer> statBonusField() {
        return StatContainer.FLOAT_CODEC.optionalFieldOf("stat_bonus", StatContainer.ZERO).forGetter(EquipmentItem::statBonus);
    }
}
