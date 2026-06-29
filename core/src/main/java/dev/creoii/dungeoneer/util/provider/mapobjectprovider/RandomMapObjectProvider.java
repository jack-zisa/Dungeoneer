package dev.creoii.dungeoneer.util.provider.mapobjectprovider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.map.tile.MapObject;
import dev.creoii.dungeoneer.util.Identifiable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class RandomMapObjectProvider implements MapObjectProvider {
    public static final MapCodec<RandomMapObjectProvider> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(RandomMapObjectProvider::id),
            MapObjectProvider.CODEC.listOf().fieldOf("values").forGetter(RandomMapObjectProvider::values)
        ).apply(instance, RandomMapObjectProvider::new)
    );
    private final String id;
    private final List<MapObjectProvider> values;

    public RandomMapObjectProvider(String id, List<MapObjectProvider> values) {
        this.id = id;
        this.values = values;
    }

    @Override
    public String id() {
        return id;
    }

    public List<MapObjectProvider> values() {
        return values;
    }

    @Override
    public Type getType() {
        return Type.RANDOM;
    }

    @Override
    @Nullable
    public MapObject get(Random random) {
        return values.get(random.nextInt(values.size())).get(random);
    }

    @Override
    public MapObject getMapObject() {
        return values.getFirst().getMapObject();
    }

    @Override
    public Identifiable withId(String id) {
        return new RandomMapObjectProvider(id, values);
    }
}
