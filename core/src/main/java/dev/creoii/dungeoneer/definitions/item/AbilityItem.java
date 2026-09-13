package dev.creoii.dungeoneer.definitions.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.action.Action;
import dev.creoii.dungeoneer.util.action.EmptyAction;
import dev.creoii.dungeoneer.util.stat.StatContainer;

public record AbilityItem(String id, EquipmentType equipmentType, String displayName, String description, Rarity rarity, StatContainer statBonus, Action activate) implements EquipmentItem {
    public static final MapCodec<AbilityItem> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
            Identifiable.idField(),
            EquipmentItem.equipmentTypeField(Type.ABILITY),
            Item.displayNameField(),
            Item.descriptionField(),
            EquipmentItem.rarityField(),
            EquipmentItem.statBonusField(),
            Action.CODEC.optionalFieldOf("action", EmptyAction.INSTANCE).forGetter(AbilityItem::activate)
        ).apply(instance, AbilityItem::new);
    });

    @Override
    public Type type() {
        return Type.ABILITY;
    }

    @Override
    public Identifiable withId(String id) {
        return new AbilityItem(id, equipmentType, displayName, description, rarity, statBonus, activate);
    }
}
