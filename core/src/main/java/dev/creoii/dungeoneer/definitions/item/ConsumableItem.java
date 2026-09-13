package dev.creoii.dungeoneer.definitions.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.util.Identifiable;

public record ConsumableItem(String id, String displayName, String description, int stackSize) implements Item {
    public static final MapCodec<ConsumableItem> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
            Identifiable.idField(),
            Item.displayNameField(),
            Item.descriptionField(),
            Codec.INT.optionalFieldOf("stack_size", 1).forGetter(ConsumableItem::stackSize)
        ).apply(instance, ConsumableItem::new);
    });

    @Override
    public Type type() {
        return Type.CONSUMABLE;
    }

    @Override
    public boolean equippable() {
        return false;
    }

    @Override
    public boolean stackable() {
        return stackSize > 1;
    }

    @Override
    public Identifiable withId(String id) {
        return new ConsumableItem(id, displayName, description, stackSize);
    }
}
