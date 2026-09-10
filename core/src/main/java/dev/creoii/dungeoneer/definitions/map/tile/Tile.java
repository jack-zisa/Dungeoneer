package dev.creoii.dungeoneer.definitions.map.tile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.util.Identifiable;

public record Tile(String id, int tileId) implements Identifiable<String> {
    public static final Codec<Tile> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(Tile::id),
            Codec.INT.fieldOf("tile_id").forGetter(Tile::tileId)
        ).apply(instance, Tile::new);
    });
    public static final Codec<Tile> ID_CODEC = Codec.STRING.xmap(DataManager::getTile, Tile::id);

    @Override
    public Identifiable<String> withId(String id) {
        return new Tile(id, tileId);
    }
}
