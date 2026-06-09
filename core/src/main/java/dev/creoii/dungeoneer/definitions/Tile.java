package dev.creoii.dungeoneer.definitions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.util.Identifiable;

public record Tile(String id, int tileId) implements Identifiable {
    public static final Codec<Tile> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.STRING.fieldOf("id").forGetter(Tile::id),
            Codec.INT.fieldOf("tile_id").forGetter(Tile::tileId)
        ).apply(instance, Tile::new);
    });

    @Override
    public String toString() {
        return id;
    }
}
