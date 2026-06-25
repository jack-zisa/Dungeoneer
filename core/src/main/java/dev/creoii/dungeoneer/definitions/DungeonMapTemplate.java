package dev.creoii.dungeoneer.definitions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.util.Identifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record DungeonMapTemplate(String id, Map<LayerType, Layer> layers) implements Identifiable {
    public static final Codec<DungeonMapTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.optionalFieldOf("id", "").forGetter(DungeonMapTemplate::id),
        Codec.unboundedMap(LayerType.CODEC, Layer.CODEC).fieldOf("layers").forGetter(DungeonMapTemplate::layers)
    ).apply(instance, DungeonMapTemplate::new));

    @Override
    public Identifiable withId(String id) {
        return new DungeonMapTemplate(id, layers);
    }

    public record Layer(Palette palette, char[][] map) {
        private static final Codec<char[][]> MAP_CODEC = Codec.list(Codec.STRING).xmap(rows -> {
            char[][] map = new char[rows.size()][];
            for (int i = 0; i < rows.size(); i++) {
                map[i] = rows.get(i).toCharArray();
            }
            return map;
            },
            map -> {
            List<String> rows = new ArrayList<>(map.length);
            for (char[] row : map) {
                rows.add(new String(row));
            }
            return rows;
        });

        public static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group(Palette.CODEC.fieldOf("palette").forGetter(Layer::palette),
            MAP_CODEC.fieldOf("map").forGetter(Layer::map)
        ).apply(instance, Layer::new));
    }

    public record Palette(Map<Character, Integer> palette) {
        public static final Codec<Palette> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING.xmap(s -> s.charAt(0), String::valueOf), Codec.INT).fieldOf("palette").forGetter(Palette::palette)
        ).apply(instance, Palette::new));
    }

    public enum LayerType {
        GROUND,
        OBJECT,
        WALL;

        public static final Codec<LayerType> CODEC = Codec.STRING.xmap(LayerType::valueOf, LayerType::name);
    }
}
