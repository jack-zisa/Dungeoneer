package dev.creoii.dungeoneer.util.provider.tileprovider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.map.tile.Tile;
import dev.creoii.dungeoneer.util.Identifiable;

import java.util.Random;

public record SimpleTileProvider(String id, Tile value) implements TileProvider {
    public static final SimpleTileProvider EMPTY = new SimpleTileProvider("empty", DataManager.getTile("stone"));
    public static final MapCodec<SimpleTileProvider> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(SimpleTileProvider::id),
            Tile.ID_CODEC.fieldOf("value").forGetter(SimpleTileProvider::value)
        ).apply(instance, SimpleTileProvider::new)
    );

    @Override
    public Type getType() {
        return Type.SIMPLE;
    }

    @Override
    public Tile get(Random random) {
        return value;
    }

    @Override
    public Tile getTile() {
        return value;
    }

    @Override
    public Identifiable withId(String id) {
        return new SimpleTileProvider(id, value);
    }
}
