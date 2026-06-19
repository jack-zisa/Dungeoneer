package dev.creoii.dungeoneer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.creoii.dungeoneer.definitions.attack.Attack;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletType;
import dev.creoii.dungeoneer.definitions.CharacterClass;
import dev.creoii.dungeoneer.definitions.Tile;
import dev.creoii.dungeoneer.definitions.attack.AttackType;
import dev.creoii.dungeoneer.definitions.attack.ReferenceAttack;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.logging.Logger;
import dev.creoii.dungeoneer.util.provider.tileprovider.TileProvider;
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

    public static Object2ObjectArrayMap<String, Identifiable> getBullets() {
        return DATA.get(SchemaType.BULLET);
    }

    public static Object2ObjectArrayMap<String, Identifiable> getAttacks() {
        return DATA.get(SchemaType.ATTACK);
    }

    public static Object2ObjectArrayMap<String, Identifiable> getClasses() {
        return DATA.get(SchemaType.CLASS);
    }

    public static Object2ObjectArrayMap<String, Identifiable> getTiles() {
        return DATA.get(SchemaType.TILE);
    }

    public static Object2ObjectArrayMap<String, Identifiable> getTileProviders() {
        return DATA.get(SchemaType.TILE_PROVIDER);
    }

    @Nullable
    public static BulletType getBullet(String id) {
        BulletType value = (BulletType) getBullets().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Unknown Bullet: '" + id + "'");
            return null;
        }
        return value;
    }

    public static Attack getAttack(String id) {
        Attack value = (Attack) getAttacks().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Creating reference attack: '" + id + "'");
            return new ReferenceAttack(id, AttackType.REFERENCE);
        }
        return value;
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

    @Nullable
    public static TileProvider getTileProvider(String id) {
        TileProvider value = (TileProvider) getTileProviders().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Unknown Tile Provider: '" + id + "'");
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
                    LOGGER.info("Folder '" + folderPath + "' does not exist, creating.");
                    Files.createDirectories(folderPath);
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
        BULLET("bullet"),
        ATTACK("attack"),
        CLASS("class"),
        TILE("tile"),
        TILE_PROVIDER("tile_provider");

        private final String path;

        SchemaType(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    static {
        SCHEMA.put(SchemaType.BULLET, BulletType.CODEC);
        SCHEMA.put(SchemaType.ATTACK, Attack.CODEC);
        SCHEMA.put(SchemaType.CLASS, CharacterClass.CODEC);
        SCHEMA.put(SchemaType.TILE, Tile.CODEC);
        SCHEMA.put(SchemaType.TILE_PROVIDER, TileProvider.CODEC);

        for (SchemaType schemaType : SCHEMA.keySet()) {
            DATA.put(schemaType, new Object2ObjectArrayMap<>());
        }
    }
}
