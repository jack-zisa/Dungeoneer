package dev.creoii.dungeoneer.definitions.map.generator;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.map.MapLayerType;
import dev.creoii.dungeoneer.definitions.map.tile.TileSetter;
import dev.creoii.dungeoneer.util.Codecs;
import dev.creoii.dungeoneer.util.Constants;
import dev.creoii.dungeoneer.util.context.Context;
import dev.creoii.dungeoneer.util.action.value.ValueType;
import dev.creoii.dungeoneer.util.provider.tileprovider.SimpleTileProvider;
import dev.creoii.dungeoneer.util.provider.tileprovider.TileProvider;
import org.jspecify.annotations.Nullable;

import java.util.Random;

public record CircleMapGenerator(Vector2 center, TileProvider tile, int radius, int thickness) implements MapGenerator {
    public static final MapCodec<CircleMapGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codecs.VECTOR_2.fieldOf("center").orElse(Vector2.Zero).forGetter(CircleMapGenerator::center),
            TileProvider.CODEC.fieldOf("tile").orElse(SimpleTileProvider.EMPTY).forGetter(CircleMapGenerator::tile),
            Codec.INT.fieldOf("radius").forGetter(CircleMapGenerator::radius),
            Codec.INT.fieldOf("thickness").orElse(0).forGetter(CircleMapGenerator::thickness)
        ).apply(instance, CircleMapGenerator::new)
    );

    @Override
    public Type getType() {
        return Type.CIRCLE;
    }

    @Override
    public void apply(@Nullable TiledMapTileLayer layer, MapLayerType layerType, long seed, Random random, TileSetter setter) {
        random.setSeed(seed);

        int centerX = Math.round(center.x);
        int centerY = Math.round(center.y);

        int outerRadius2 = radius * radius;
        int innerRadius = Math.max(0, radius - thickness);
        int innerRadius2 = innerRadius * innerRadius;

        int minX = Math.max(0, centerX - radius);
        int maxX = Math.min(Constants.MAP_WIDTH - 1, centerX + radius);
        int minY = Math.max(0, centerY - radius);
        int maxY = Math.min(Constants.MAP_HEIGHT - 1, centerY + radius);

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {

                int dx = x - centerX;
                int dy = y - centerY;
                int dist2 = dx * dx + dy * dy;

                if (dist2 > outerRadius2) {
                    continue;
                }

                if (thickness > 0 && dist2 < innerRadius2) {
                    continue;
                }

                Context context = new Context()
                    .set(ValueType.RANDOM, random)
                    .set(ValueType.POSITION, new Vector2(x, y))
                    .set(ValueType.SEED, seed);

                setter.set(layer, x, y, tile.get(context).id());
            }
        }
    }
}
