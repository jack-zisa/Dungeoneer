package dev.creoii.dungeoneer.util.provider.tileprovider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.map.tile.Tile;
import dev.creoii.dungeoneer.util.Identifiable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class RandomTileProvider implements TileProvider {
    public static final MapCodec<RandomTileProvider> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(RandomTileProvider::id),
            TileProvider.CODEC.listOf().fieldOf("values").forGetter(RandomTileProvider::values)
        ).apply(instance, RandomTileProvider::new)
    );
    private final String id;
    private final List<TileProvider> values;

    public RandomTileProvider(String id, List<TileProvider> values) {
        this.id = id;
        this.values = values;
    }

    @Override
    public String id() {
        return id;
    }

    public List<TileProvider> values() {
        return values;
    }

    @Override
    public Type getType() {
        return Type.RANDOM;
    }

    @Override
    @Nullable
    public Tile get(Random random) {
        return values.get(random.nextInt(values.size())).get(random);
    }

    @Override
    public Tile getTile() {
        return values.getFirst().getTile();
    }

    @Override
    public Identifiable withId(String id) {
        return new RandomTileProvider(id, values);
    }
}
