package dev.creoii.dungeoneer.definitions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.stat.StatContainer;

public record CharacterClass(String id, StatContainer baseStats, StatContainer maxStats) implements Identifiable<String> {
    public static final Codec<CharacterClass> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(CharacterClass::id),
            StatContainer.INT_CODEC.fieldOf("base_stats").orElse(new StatContainer()).forGetter(CharacterClass::baseStats),
            StatContainer.INT_CODEC.fieldOf("max_stats").orElse(new StatContainer()).forGetter(CharacterClass::maxStats)
        ).apply(instance, CharacterClass::new);
    });
    public static final Codec<CharacterClass> ID_CODEC = Codec.STRING.xmap(DataManager::getCharacterClass, CharacterClass::id);

    @Override
    public Identifiable<String> withId(String id) {
        return new CharacterClass(id, baseStats, maxStats);
    }
}
