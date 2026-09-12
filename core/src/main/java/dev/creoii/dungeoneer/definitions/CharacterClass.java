package dev.creoii.dungeoneer.definitions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.item.EquipmentItem;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.stat.StatContainer;

public record CharacterClass(String id, ClassEquipment equipment, StatContainer baseStats, StatContainer maxStats) implements Identifiable {
    public static final Codec<CharacterClass> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(CharacterClass::id),
            ClassEquipment.CODEC.fieldOf("equipment").forGetter(CharacterClass::equipment),
            StatContainer.FLOAT_CODEC.fieldOf("base_stats").orElse(new StatContainer()).forGetter(CharacterClass::baseStats),
            StatContainer.FLOAT_CODEC.fieldOf("max_stats").orElse(new StatContainer()).forGetter(CharacterClass::maxStats)
        ).apply(instance, CharacterClass::new);
    });
    public static final Codec<CharacterClass> ID_CODEC = Codec.STRING.xmap(DataManager::getCharacterClass, CharacterClass::id);

    @Override
    public Identifiable withId(String id) {
        return new CharacterClass(id, equipment, baseStats, maxStats);
    }

    public record ClassEquipment(EquipmentItem.EquipmentType weapon, EquipmentItem.EquipmentType ability, EquipmentItem.EquipmentType armor, EquipmentItem.EquipmentType accessory) {
        public static final Codec<ClassEquipment> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                EquipmentItem.EquipmentType.CODEC.fieldOf("weapon").forGetter(ClassEquipment::weapon),
                EquipmentItem.EquipmentType.CODEC.fieldOf("ability").forGetter(ClassEquipment::ability),
                EquipmentItem.EquipmentType.CODEC.fieldOf("armor").forGetter(ClassEquipment::armor),
                EquipmentItem.EquipmentType.CODEC.fieldOf("accessory").forGetter(ClassEquipment::accessory)
            ).apply(instance, ClassEquipment::new);
        });
    }
}
