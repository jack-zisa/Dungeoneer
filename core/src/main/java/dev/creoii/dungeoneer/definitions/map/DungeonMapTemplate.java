package dev.creoii.dungeoneer.definitions.map;

import com.badlogic.gdx.math.Vector2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.map.generator.MapGenerator;
import dev.creoii.dungeoneer.util.Codecs;
import dev.creoii.dungeoneer.util.Identifiable;

import java.util.Map;

public record DungeonMapTemplate(String id, Vector2 spawnPos, Map<LayerType, MapGenerator> layers) implements Identifiable<String> {
    public static final Codec<DungeonMapTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.optionalFieldOf("id", "").forGetter(DungeonMapTemplate::id),
        Codecs.VECTOR_2.fieldOf("spawn_pos").forGetter(DungeonMapTemplate::spawnPos),
        Codec.unboundedMap(LayerType.CODEC, MapGenerator.TYPE_CODEC).fieldOf("layers").forGetter(DungeonMapTemplate::layers)
    ).apply(instance, DungeonMapTemplate::new));

    @Override
    public Identifiable<String> withId(String id) {
        return new DungeonMapTemplate(id, spawnPos, layers);
    }

    public enum LayerType {
        GROUND,
        OBJECT,
        WALL;

        public static final Codec<LayerType> CODEC = Codec.STRING.xmap(s -> LayerType.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    }
}
