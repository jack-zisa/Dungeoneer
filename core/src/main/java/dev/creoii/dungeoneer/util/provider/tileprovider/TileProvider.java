package dev.creoii.dungeoneer.util.provider.tileprovider;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.creoii.dungeoneer.definitions.map.tile.Tile;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.provider.Provider;

import java.util.function.Function;

public interface TileProvider extends Provider<Tile>, Identifiable {
    Codec<TileProvider> TYPE_CODEC = Type.CODEC.dispatch(TileProvider::getType, type -> switch (type) {
        case SIMPLE -> SimpleTileProvider.CODEC;
        case RANDOM -> RandomTileProvider.CODEC;
    });
    Codec<TileProvider> CODEC = Codec.either(Tile.ID_CODEC, TYPE_CODEC).xmap(either -> {
        return either.map(tile -> new SimpleTileProvider(tile.id(), tile), Function.identity());
    }, Either::right);

    Type getType();

    Tile getTile();

    enum Type {
        SIMPLE,
        RANDOM;

        public static final Codec<Type> CODEC = Codec.STRING.xmap(s -> Type.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    }
}
