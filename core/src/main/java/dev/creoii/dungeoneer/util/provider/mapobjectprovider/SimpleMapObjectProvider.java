package dev.creoii.dungeoneer.util.provider.mapobjectprovider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.map.tile.MapObject;
import dev.creoii.dungeoneer.util.context.Context;
import dev.creoii.dungeoneer.util.Identifiable;

public record SimpleMapObjectProvider(String id, MapObject value) implements MapObjectProvider {
    public static final SimpleMapObjectProvider EMPTY = new SimpleMapObjectProvider("empty", DataManager.getMapObject("short_grass"));
    public static final MapCodec<SimpleMapObjectProvider> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(SimpleMapObjectProvider::id),
            MapObject.ID_CODEC.fieldOf("value").forGetter(SimpleMapObjectProvider::value)
        ).apply(instance, SimpleMapObjectProvider::new)
    );

    @Override
    public Type getType() {
        return Type.SIMPLE;
    }

    @Override
    public MapObject get(Context context) {
        return value;
    }

    @Override
    public MapObject getMapObject() {
        return value;
    }

    @Override
    public Identifiable withId(String id) {
        return new SimpleMapObjectProvider(id, value);
    }
}
