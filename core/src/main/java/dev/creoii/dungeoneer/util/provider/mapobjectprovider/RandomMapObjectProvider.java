package dev.creoii.dungeoneer.util.provider.mapobjectprovider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.map.tile.MapObject;
import dev.creoii.dungeoneer.util.context.Context;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.action.value.ValueType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public record RandomMapObjectProvider(String id, List<MapObjectProvider> values) implements MapObjectProvider {
    public static final MapCodec<RandomMapObjectProvider> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(RandomMapObjectProvider::id),
            MapObjectProvider.CODEC.listOf().fieldOf("values").forGetter(RandomMapObjectProvider::values)
        ).apply(instance, RandomMapObjectProvider::new)
    );

    @Override
    public Type getType() {
        return Type.RANDOM;
    }

    @Override
    @Nullable
    public MapObject get(Context context) {
        if (context.has(ValueType.RANDOM)) {
            Random random = context.get(ValueType.RANDOM);
            return values.get(random.nextInt(values.size())).get(context);
        }
        throw new IllegalStateException("Cannot call get() on a RandomMapObjectProvider with no random context.");
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
