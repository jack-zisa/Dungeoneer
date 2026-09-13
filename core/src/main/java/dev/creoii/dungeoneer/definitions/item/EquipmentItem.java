package dev.creoii.dungeoneer.definitions.item;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.util.stat.StatContainer;

public sealed interface EquipmentItem extends Item permits AbilityItem, AccessoryItem, ArmorItem, WeaponItem {
    EquipmentType equipmentType();

    Rarity rarity();

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

    static <T extends EquipmentItem> RecordCodecBuilder<T, Rarity> rarityField() {
        return Rarity.CODEC.optionalFieldOf("rarity", Rarity.COMMON).forGetter(EquipmentItem::rarity);
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

    enum Rarity {
        COMMON(Color.WHITE),
        UNCOMMON(Color.GREEN),
        RARE(Color.BLUE),
        LEGENDARY(Color.PURPLE),
        MYTHICAL(Color.GOLD);

        public static final Codec<Rarity> CODEC = Codec.STRING.xmap(s -> Rarity.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
        private final Color color;

        Rarity(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }
    }
}
