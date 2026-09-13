package dev.creoii.dungeoneer.definitions.map.tile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.util.Identifiable;

public record Tile(String id, float speedMultiplier) implements Identifiable {
    public static final Codec<Tile> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(Tile::id),
            Codec.FLOAT.optionalFieldOf("speed_multiplier", 1f).forGetter(Tile::speedMultiplier)
        ).apply(instance, Tile::new);
    });
    public static final Codec<Tile> ID_CODEC = Codec.STRING.xmap(DataManager::getTile, Tile::id);

    @Override
    public Identifiable withId(String id) {
        return new Tile(id, speedMultiplier);
    }
}
