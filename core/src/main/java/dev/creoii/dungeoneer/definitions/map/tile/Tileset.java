package dev.creoii.dungeoneer.definitions.map.tile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.provider.tileprovider.TileProvider;

public record Tileset(String id, TileProvider ground, TileProvider wall) implements Identifiable {
    public static final Codec<Tileset> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(Tileset::id),
            TileProvider.CODEC.fieldOf("ground").forGetter(Tileset::ground),
            TileProvider.CODEC.fieldOf("wall").forGetter(Tileset::wall)
        ).apply(instance, Tileset::new);
    });

    @Override
    public Identifiable withId(String id) {
        return new Tileset(id, ground, wall);
    }
}
