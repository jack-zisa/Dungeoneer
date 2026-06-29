package dev.creoii.dungeoneer.util.provider.mapobjectprovider;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.creoii.dungeoneer.definitions.map.tile.MapObject;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.provider.MapObjectContext;
import dev.creoii.dungeoneer.util.provider.Provider;

import java.util.function.Function;

public interface MapObjectProvider extends Provider<MapObject, MapObjectContext>, Identifiable {
    Codec<MapObjectProvider> TYPE_CODEC = Type.CODEC.dispatch(MapObjectProvider::getType, type -> switch (type) {
        case SIMPLE -> SimpleMapObjectProvider.CODEC;
        case RANDOM -> RandomMapObjectProvider.CODEC;
    });
    Codec<MapObjectProvider> CODEC = Codec.either(MapObject.ID_CODEC, TYPE_CODEC).xmap(either -> {
        return either.map(mapObject -> new SimpleMapObjectProvider(mapObject.id(), mapObject), Function.identity());
    }, Either::right);

    Type getType();

    MapObject getMapObject();

    enum Type {
        SIMPLE,
        RANDOM;

        public static final Codec<Type> CODEC = Codec.STRING.xmap(s -> Type.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    }
}
