package dev.creoii.dungeoneer.definitions.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.map.generator.MapGenerator;
import dev.creoii.dungeoneer.util.Identifiable;

import java.util.Map;

public record DungeonMapTemplate(String id, Map<LayerType, MapGenerator> layers) implements Identifiable {
    public static final Codec<DungeonMapTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.optionalFieldOf("id", "").forGetter(DungeonMapTemplate::id),
        Codec.unboundedMap(LayerType.CODEC, MapGenerator.TYPE_CODEC).fieldOf("layers").forGetter(DungeonMapTemplate::layers)
    ).apply(instance, DungeonMapTemplate::new));

    @Override
    public Identifiable withId(String id) {
        return new DungeonMapTemplate(id, layers);
    }

    public enum LayerType {
        GROUND,
        OBJECT,
        WALL;

        public static final Codec<LayerType> CODEC = Codec.STRING.xmap(s -> LayerType.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    }
}
