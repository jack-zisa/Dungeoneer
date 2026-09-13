package dev.creoii.dungeoneer.definitions.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.util.Identifiable;

import java.util.function.Function;

public sealed interface Item extends Identifiable permits EquipmentItem, ConsumableItem {
    Type type();

    String displayName();

    boolean equippable();

    boolean stackable();

    Codec<Item> CODEC = Type.CODEC.dispatch(Item::type, type -> switch (type) {
        case WEAPON -> WeaponItem.TYPE_CODEC;
        case ABILITY -> AbilityItem.TYPE_CODEC;
        case ARMOR -> ArmorItem.TYPE_CODEC;
        case ACCESSORY -> AccessoryItem.TYPE_CODEC;
        case CONSUMABLE -> ConsumableItem.TYPE_CODEC;
    });
    Codec<Item> EITHER_CODEC = Codec.either(Codec.STRING, CODEC).xmap(either -> {
        return either.map(DataManager::getItem, Function.identity());
    }, Either::right);

    static <T extends Item> RecordCodecBuilder<T, String> displayNameField() {
        return Codec.STRING.fieldOf("display_name").forGetter(Item::displayName);
    }

    enum Type {
        WEAPON,
        ABILITY,
        ARMOR,
        ACCESSORY,
        CONSUMABLE;

        public static final Codec<Type> CODEC = Codec.STRING.xmap(s -> Type.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    }
}
