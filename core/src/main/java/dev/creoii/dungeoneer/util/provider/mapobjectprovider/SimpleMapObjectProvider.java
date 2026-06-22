package dev.creoii.dungeoneer.util.provider.mapobjectprovider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.MapObject;

import java.util.Random;

public record SimpleMapObjectProvider(String id, MapObject value) implements MapObjectProvider {
    public static final SimpleMapObjectProvider EMPTY = new SimpleMapObjectProvider("empty", DataManager.getMapObject("short_grass"));
    public static final MapCodec<SimpleMapObjectProvider> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.STRING.fieldOf("id").forGetter(SimpleMapObjectProvider::id),
            MapObject.ID_CODEC.fieldOf("value").forGetter(SimpleMapObjectProvider::value)
        ).apply(instance, SimpleMapObjectProvider::new)
    );

    @Override
    public Type getType() {
        return Type.SIMPLE;
    }

    @Override
    public MapObject get(Random random) {
        return value;
    }

    @Override
    public MapObject getMapObject() {
        return value;
    }
}
