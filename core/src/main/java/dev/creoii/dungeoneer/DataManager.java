package dev.creoii.dungeoneer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.creoii.dungeoneer.definitions.CharacterClass;
import dev.creoii.dungeoneer.definitions.Tile;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.logging.Logger;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import javax.annotation.Nullable;
import java.io.FileReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

public class DataManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Logger LOGGER = new Logger(DataManager.class.getSimpleName());
    private static final EnumMap<SchemaType, Codec<? extends Identifiable>> SCHEMA = new EnumMap<>(SchemaType.class);
    private static final EnumMap<SchemaType, Object2ObjectArrayMap<String, Identifiable>> DATA = new EnumMap<>(SchemaType.class);
    private static boolean DEBUG = false;

    public static void setDebug(boolean debug) {
        DataManager.DEBUG = debug;
    }

    public static Object2ObjectArrayMap<String, Identifiable> getClasses() {
        return DATA.get(SchemaType.CLASS);
    }

    public static Object2ObjectArrayMap<String, Identifiable> getTiles() {
        return DATA.get(SchemaType.TILE);
    }

    @Nullable
    public static CharacterClass getCharacterClass(String id) {
        CharacterClass value = (CharacterClass) getClasses().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Unknown CharacterClass: '" + id + "'");
            return null;
        }
        return value;
    }

    @Nullable
    public static Tile getTile(String id) {
        Tile value = (Tile) getTiles().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Unknown Tile: '" + id + "'");
            return null;
        }
        return value;
    }

    public static void load(Path path) {
        try {
            for (Map.Entry<SchemaType, Codec<? extends Identifiable>> entry : SCHEMA.entrySet()) {
                String folder = entry.getKey().getPath();
                Codec<?> codec = entry.getValue();

                Path folderPath = path.resolve(folder);
                if (!Files.exists(folderPath)) {
                    LOGGER.info("Folder '" + folderPath + "' does not exist, skipping.");
                    continue;
                }

                Object2ObjectArrayMap<String, Identifiable> data = DATA.get(entry.getKey());
                try (Stream<Path> paths = Files.walk(folderPath)) {
                    Stream<Path> filtered = paths.filter(p -> p.toString().endsWith(".json"));
                    for (Path file : filtered.toList()) {
                        try (Reader reader = new FileReader(file.toFile())) {
                            JsonElement jsonValue = GSON.fromJson(reader, JsonElement.class);
                            DataResult<?> result = codec.parse(JsonOps.INSTANCE, jsonValue);

                            if (result.error().isPresent()) {
                                LOGGER.error("Error parsing " + file.getFileName() + " in '/" + folder + "': " + result.error().get().message());
                                continue;
                            }

                            Identifiable obj = (Identifiable) result.getOrThrow();
                            data.put(obj.id(), obj);
                        } catch (Exception e) {
                            LOGGER.error("Error parsing " + file.getFileName() + " in '/" + folder + "': " + e);
                        }
                    }
                }

                LOGGER.info("Loaded " + data.size() + " object(s) for type " + folder);
            }
        } catch (Exception e) {
            LOGGER.error("Error loading data: " + e);
        }
    }

    public static void load() {
        URL baseUrl = DataManager.class.getClassLoader().getResource("dungeoneer/data");
        if (baseUrl == null) {
            LOGGER.error("Directory 'data/' does not exist");
            return;
        }

        Path path = null;

        try {
            path = Paths.get(baseUrl.toURI());
        } catch (URISyntaxException e) {
            LOGGER.info("Folder '" + path + "' does not exist, skipping.");
            return;
        }

        load(path);
    }

    public enum SchemaType {
        CLASS("class"),
        TILE("tile");

        private final String path;

        SchemaType(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    static {
        SCHEMA.put(SchemaType.CLASS, CharacterClass.CODEC);
        SCHEMA.put(SchemaType.TILE, Tile.CODEC);

        for (SchemaType schemaType : SCHEMA.keySet()) {
            DATA.put(schemaType, new Object2ObjectArrayMap<>());
        }
    }
}
