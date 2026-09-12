package dev.creoii.dungeoneer.definitions.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.stat.StatContainer;

public record AccessoryItem(String id, StatContainer statBonus) implements EquipmentItem {
    public static final MapCodec<AccessoryItem> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
            Identifiable.idField(),
            EquipmentItem.statBonusField()
        ).apply(instance, AccessoryItem::new);
    });

    @Override
    public Type type() {
        return Type.ACCESSORY;
    }

    @Override
    public Identifiable withId(String id) {
        return new AccessoryItem(id, statBonus);
    }
}
