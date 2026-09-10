package dev.creoii.dungeoneer.definitions.map;

import com.badlogic.gdx.math.Vector2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.map.generator.MapGenerator;
import dev.creoii.dungeoneer.util.Codecs;
import dev.creoii.dungeoneer.util.Identifiable;

import java.util.Map;

public record DungeonMapTemplate(String id, Vector2 spawnPos, Map<MapLayerType, MapGenerator> layers) implements Identifiable {
    public static final Codec<DungeonMapTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.optionalFieldOf("id", "").forGetter(DungeonMapTemplate::id),
        Codecs.VECTOR_2.fieldOf("spawn_pos").forGetter(DungeonMapTemplate::spawnPos),
        Codec.unboundedMap(MapLayerType.CODEC, MapGenerator.TYPE_CODEC).fieldOf("layers").forGetter(DungeonMapTemplate::layers)
    ).apply(instance, DungeonMapTemplate::new));

    @Override
    public Identifiable withId(String id) {
        return new DungeonMapTemplate(id, spawnPos, layers);
    }
}
