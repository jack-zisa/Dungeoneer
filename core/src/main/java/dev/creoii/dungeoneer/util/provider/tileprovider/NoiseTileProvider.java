package dev.creoii.dungeoneer.util.provider.tileprovider;

import com.badlogic.gdx.math.Vector2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.map.tile.Tile;
import dev.creoii.dungeoneer.util.Context;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.action.value.ValueType;
import dev.creoii.dungeoneer.util.noise.FastNoiseLite;
import dev.creoii.dungeoneer.util.noise.FastNoiseParameters;

import javax.annotation.Nullable;
import java.util.List;

public class NoiseTileProvider implements TileProvider {
    public static final MapCodec<NoiseTileProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.optionalFieldOf("id", "").forGetter(NoiseTileProvider::id),
        FastNoiseParameters.CODEC.fieldOf("noise").forGetter(NoiseTileProvider::noiseParameters),
        Entry.CODEC.listOf().fieldOf("entries").forGetter(NoiseTileProvider::entries),
        TileProvider.CODEC.optionalFieldOf("fallback", SimpleTileProvider.EMPTY).forGetter(NoiseTileProvider::fallback)
    ).apply(instance, NoiseTileProvider::new));
    private final String id;
    private final FastNoiseParameters noiseParameters;
    private final FastNoiseLite noise;
    private final List<Entry> entries;
    private final TileProvider fallback;

    public NoiseTileProvider(String id, FastNoiseParameters noiseParameters, List<Entry> entries, TileProvider fallback) {
        this.id = id;
        this.noiseParameters = noiseParameters;
        this.entries = List.copyOf(entries);
        this.fallback = fallback;
        noise = new FastNoiseLite(noiseParameters);
    }

    @Override
    public String id() {
        return id;
    }

    public FastNoiseParameters noiseParameters() {
        return noiseParameters;
    }

    public List<Entry> entries() {
        return entries;
    }

    public TileProvider fallback() {
        return fallback;
    }

    @Override
    public Type getType() {
        return Type.NOISE;
    }

    @Override
    @Nullable
    public Tile get(Context context) {
        if (context.has(ValueType.SEED, ValueType.POSITION)) {
            noise.seed(context.get(ValueType.SEED));
            Vector2 pos = context.get(ValueType.POSITION);
            float value = noise.getNoise(pos.x, pos.y);
            for (Entry entry : entries) {
                if (value <= entry.max()) {
                    Tile tile = entry.tile().get(context);
                    if (tile != null) {
                        return tile;
                    }
                }
            }
        }
        return fallback.get(context);
    }

    @Override
    public Tile getTile() {
        return entries.getFirst().tile().getTile();
    }

    @Override
    public Identifiable withId(String id) {
        return new NoiseTileProvider(id, noiseParameters, entries, fallback);
    }

    public record Entry(float max, TileProvider tile) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("max").forGetter(Entry::max),
            TileProvider.CODEC.fieldOf("tile").forGetter(Entry::tile)
        ).apply(instance, Entry::new));
    }
}
