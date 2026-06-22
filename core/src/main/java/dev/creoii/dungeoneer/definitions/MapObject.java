package dev.creoii.dungeoneer.definitions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.util.Identifiable;

public record MapObject(String id, int tileId) implements Identifiable {
    public static final Codec<MapObject> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.STRING.fieldOf("id").forGetter(MapObject::id),
            Codec.INT.fieldOf("tile_id").forGetter(MapObject::tileId)
        ).apply(instance, MapObject::new);
    });
    public static final Codec<MapObject> ID_CODEC = Codec.STRING.xmap(DataManager::getMapObject, MapObject::id);

    @Override
    public String toString() {
        return id;
    }
}
