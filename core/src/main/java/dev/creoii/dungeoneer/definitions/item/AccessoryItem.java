package dev.creoii.dungeoneer.definitions.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.stat.StatContainer;

public record AccessoryItem(String id, EquipmentType equipmentType, String displayName, String description, Rarity rarity, StatContainer statBonus) implements EquipmentItem {
    public static final MapCodec<AccessoryItem> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
            Identifiable.idField(),
            EquipmentItem.equipmentTypeField(Type.ACCESSORY),
            Item.displayNameField(),
            Item.descriptionField(),
            EquipmentItem.rarityField(),
            EquipmentItem.statBonusField()
        ).apply(instance, AccessoryItem::new);
    });

    @Override
    public Type type() {
        return Type.ACCESSORY;
    }

    @Override
    public Identifiable withId(String id) {
        return new AccessoryItem(id, equipmentType, displayName, description, rarity, statBonus);
    }
}
