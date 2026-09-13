package dev.creoii.dungeoneer.util.provider.tileprovider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.map.tile.Tile;
import dev.creoii.dungeoneer.util.Context;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.action.value.ValueType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public record RandomTileProvider(String id, List<TileProvider> values) implements TileProvider {
    public static final MapCodec<RandomTileProvider> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(RandomTileProvider::id),
            TileProvider.CODEC.listOf().fieldOf("values").forGetter(RandomTileProvider::values)
        ).apply(instance, RandomTileProvider::new)
    );

    @Override
    public Type getType() {
        return Type.RANDOM;
    }

    @Override
    @Nullable
    public Tile get(Context context) {
        if (context.has(ValueType.RANDOM)) {
            Random random = context.get(ValueType.RANDOM);
            return values.get(random.nextInt(values.size())).get(context);
        }
        throw new IllegalStateException("Cannot call get() on a RandomTileProvider with no random context.");
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
