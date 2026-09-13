package dev.creoii.dungeoneer.definitions.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.stat.StatContainer;

public record ArmorItem(String id, EquipmentType equipmentType, String displayName, StatContainer statBonus) implements EquipmentItem {
    public static final MapCodec<ArmorItem> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
            Identifiable.idField(),
            EquipmentItem.equipmentTypeField(Type.ARMOR),
            Item.displayNameField(),
            EquipmentItem.statBonusField()
        ).apply(instance, ArmorItem::new);
    });

    @Override
    public Type type() {
        return Type.ARMOR;
    }

    @Override
    public Identifiable withId(String id) {
        return new ArmorItem(id, equipmentType, displayName, statBonus);
    }
}
