package dev.creoii.dungeoneer.definitions.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.util.stat.StatContainer;

public sealed interface EquipmentItem extends Item permits AbilityItem, AccessoryItem, ArmorItem, WeaponItem {
    EquipmentType equipmentType();

    StatContainer statBonus();

    @Override
    default boolean equippable() {
        return true;
    }

    @Override
    default boolean stackable() {
        return false;
    }

    static <T extends EquipmentItem> RecordCodecBuilder<T, EquipmentType> equipmentTypeField(Type type) {
        return EquipmentType.CODEC.fieldOf("equipment_type").validate(equipmentType -> equipmentType.getType() == type ? DataResult.success(equipmentType) : DataResult.error(() -> "Equipment type not applicable to item type")).forGetter(EquipmentItem::equipmentType);
    }

    static <T extends EquipmentItem> RecordCodecBuilder<T, StatContainer> statBonusField() {
        return StatContainer.FLOAT_CODEC.optionalFieldOf("stat_bonus", StatContainer.ZERO).forGetter(EquipmentItem::statBonus);
    }

    enum EquipmentType {
        SWORD(Type.WEAPON),
        SHIELD(Type.ABILITY),
        HEAVY_ARMOR(Type.ARMOR),
        RING(Type.ACCESSORY);

        public static final Codec<EquipmentType> CODEC = Codec.STRING.xmap(s -> EquipmentType.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
        private final Type type;

        EquipmentType(Type type) {
            this.type = type;
        }

        public Type getType() {
            return type;
        }
    }
}
