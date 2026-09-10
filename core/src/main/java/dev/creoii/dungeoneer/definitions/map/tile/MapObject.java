package dev.creoii.dungeoneer.definitions.map.tile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.util.Identifiable;

public record MapObject(String id) implements Identifiable {
    public static final Codec<MapObject> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(MapObject::id)
        ).apply(instance, MapObject::new);
    });
    public static final Codec<MapObject> ID_CODEC = Codec.STRING.xmap(DataManager::getMapObject, MapObject::id);

    @Override
    public Identifiable withId(String id) {
        return new MapObject(id);
    }
}
