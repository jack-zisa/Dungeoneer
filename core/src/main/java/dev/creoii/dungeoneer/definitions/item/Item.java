package dev.creoii.dungeoneer.definitions.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.util.Identifiable;

import java.util.function.Function;

public sealed interface Item extends Identifiable permits EquipmentItem, ConsumableItem {
    Type type();

    boolean equippable();

    boolean stackable();

    Codec<Item> CODEC = Type.CODEC.dispatch(Item::type, type -> switch (type) {
        case WEAPON -> null;
        case ABILITY -> null;
        case ARMOR -> null;
        case ACCESSORY -> null;
        case CONSUMABLE -> ConsumableItem.TYPE_CODEC;
    });
    Codec<Item> EITHER_CODEC = Codec.either(Codec.STRING, CODEC).xmap(either -> {
        return either.map(DataManager::getItem, Function.identity());
    }, Either::right);

    enum Type {
        WEAPON,
        ABILITY,
        ARMOR,
        ACCESSORY,
        CONSUMABLE;

        public static final Codec<Type> CODEC = Codec.STRING.xmap(s -> Type.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    }
}
