package dev.creoii.dungeoneer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.creoii.dungeoneer.definitions.*;
import dev.creoii.dungeoneer.definitions.attack.Attack;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletType;
import dev.creoii.dungeoneer.definitions.attack.ReferenceAttack;
import dev.creoii.dungeoneer.definitions.item.Item;
import dev.creoii.dungeoneer.definitions.map.DungeonMapTemplate;
import dev.creoii.dungeoneer.definitions.map.tile.MapObject;
import dev.creoii.dungeoneer.definitions.map.tile.Tile;
import dev.creoii.dungeoneer.definitions.map.tile.Tileset;
import dev.creoii.dungeoneer.definitions.statuseffect.StatusEffect;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.logging.Logger;
import dev.creoii.dungeoneer.util.noise.FastNoiseParameters;
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
    private static final EnumMap<SchemaType, Object2ObjectArrayMap<String, Identifiable>> STRING_ID_DATA = new EnumMap<>(SchemaType.class);
    private static final EnumMap<SchemaType, Object2ObjectArrayMap<Long, Identifiable>> INTERNAL_ID_DATA = new EnumMap<>(SchemaType.class);
    private static final EnumMap<SchemaType, BiMap<Long, String>> IDS = new EnumMap<>(SchemaType.class);
    private static boolean DEBUG = false;

    public static void setDebug(boolean debug) {
        DataManager.DEBUG = debug;
    }

    public static long getInternalId(SchemaType type, String id) {
        return IDS.get(type).inverse().get(id);
    }

    public static Object2ObjectArrayMap<String, Identifiable> getBullets() {
        return STRING_ID_DATA.get(SchemaType.BULLET);
    }

    public static Object2ObjectArrayMap<String, Identifiable> getAttacks() {
        return STRING_ID_DATA.get(SchemaType.ATTACK);
    }

    public static Object2ObjectArrayMap<String, Identifiable> getClasses() {
        return STRING_ID_DATA.get(SchemaType.CLASS);
    }

    public static Object2ObjectArrayMap<String, Identifiable> getTiles() {
        return STRING_ID_DATA.get(SchemaType.TILE);
    }

    public static Object2ObjectArrayMap<String, Identifiable> getTileProviders() {
        return STRING_ID_DATA.get(SchemaType.TILE_PROVIDER);
    }

    public static Object2ObjectArrayMap<String, Identifiable> getMapObjects() {
        return STRING_ID_DATA.get(SchemaType.MAP_OBJECT);
    }

    public static Object2ObjectArrayMap<String, Identifiable> getTilesets() {
        return STRING_ID_DATA.get(SchemaType.TILESET);
    }

    public static Object2ObjectArrayMap<String, Identifiable> getMapTemplates() {
        return STRING_ID_DATA.get(SchemaType.MAP_TEMPLATE);
    }

    public static Object2ObjectArrayMap<String, Identifiable> getNoiseParameters() {
        return STRING_ID_DATA.get(SchemaType.NOISE_PARAMETERS);
    }

    public static Object2ObjectArrayMap<String, Identifiable> getStatusEffects() {
        return STRING_ID_DATA.get(SchemaType.STATUS_EFFECT);
    }

    public static Object2ObjectArrayMap<String, Identifiable> getItems() {
        return STRING_ID_DATA.get(SchemaType.ITEM);
    }

    public static Object2ObjectArrayMap<Long, Identifiable> getBulletsInternal() {
        return INTERNAL_ID_DATA.get(SchemaType.BULLET);
    }

    public static Object2ObjectArrayMap<Long, Identifiable> getAttacksInternal() {
        return INTERNAL_ID_DATA.get(SchemaType.ATTACK);
    }

    public static Object2ObjectArrayMap<Long, Identifiable> getClassesInternal() {
        return INTERNAL_ID_DATA.get(SchemaType.CLASS);
    }

    public static Object2ObjectArrayMap<Long, Identifiable> getTilesInternal() {
        return INTERNAL_ID_DATA.get(SchemaType.TILE);
    }

    public static Object2ObjectArrayMap<Long, Identifiable> getTileProvidersInternal() {
        return INTERNAL_ID_DATA.get(SchemaType.TILE_PROVIDER);
    }

    public static Object2ObjectArrayMap<Long, Identifiable> getMapObjectsInternal() {
        return INTERNAL_ID_DATA.get(SchemaType.MAP_OBJECT);
    }

    public static Object2ObjectArrayMap<Long, Identifiable> getTilesetsInternal() {
        return INTERNAL_ID_DATA.get(SchemaType.TILESET);
    }

    public static Object2ObjectArrayMap<Long, Identifiable> getMapTemplatesInternal() {
        return INTERNAL_ID_DATA.get(SchemaType.MAP_TEMPLATE);
    }

    public static Object2ObjectArrayMap<Long, Identifiable> getNoiseParametersInternal() {
        return INTERNAL_ID_DATA.get(SchemaType.NOISE_PARAMETERS);
    }

    public static Object2ObjectArrayMap<Long, Identifiable> getStatusEffectsInternal() {
        return INTERNAL_ID_DATA.get(SchemaType.STATUS_EFFECT);
    }

    public static Object2ObjectArrayMap<Long, Identifiable> getItemsInternal() {
        return INTERNAL_ID_DATA.get(SchemaType.ITEM);
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
            return new ReferenceAttack(id);
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

    @Nullable
    public static MapObject getMapObject(String id) {
        MapObject value = (MapObject) getMapObjects().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Unknown Map Object: '" + id + "'");
            return null;
        }
        return value;
    }

    @Nullable
    public static Tileset getTileset(String id) {
        Tileset value = (Tileset) getTilesets().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Unknown Tileset: '" + id + "'");
            return null;
        }
        return value;
    }

    @Nullable
    public static DungeonMapTemplate getMapTemplate(String id) {
        DungeonMapTemplate value = (DungeonMapTemplate) getMapTemplates().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Unknown Map Template: '" + id + "'");
            return null;
        }
        return value;
    }

    @Nullable
    public static FastNoiseParameters getNoiseParameters(String id) {
        FastNoiseParameters value = (FastNoiseParameters) getNoiseParameters().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Unknown Noise Parameters: '" + id + "'");
            return null;
        }
        return value;
    }

    @Nullable
    public static StatusEffect getStatusEffect(String id) {
        StatusEffect value = (StatusEffect) getStatusEffects().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Unknown Status Effect: '" + id + "'");
            return null;
        }
        return value;
    }

    @Nullable
    public static Item getItem(String id) {
        Item value = (Item) getItems().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Unknown Item: '" + id + "'");
            return null;
        }
        return value;
    }

    @Nullable
    public static BulletType getBullet(long id) {
        BulletType value = (BulletType) getBulletsInternal().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Unknown Bullet: '" + id + "'");
            return null;
        }
        return value;
    }

    public static Attack getAttack(long id) {
        Attack value = (Attack) getAttacksInternal().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Creating reference attack: '" + id + "'");
            return new ReferenceAttack(IDS.get(SchemaType.ATTACK).get(id));
        }
        return value;
    }

    @Nullable
    public static CharacterClass getCharacterClass(long id) {
        CharacterClass value = (CharacterClass) getClassesInternal().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Unknown CharacterClass: '" + id + "'");
            return null;
        }
        return value;
    }

    @Nullable
    public static Tile getTile(long id) {
        Tile value = (Tile) getTilesInternal().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Unknown Tile: '" + id + "'");
            return null;
        }
        return value;
    }

    @Nullable
    public static TileProvider getTileProvider(long id) {
        TileProvider value = (TileProvider) getTileProvidersInternal().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Unknown Tile Provider: '" + id + "'");
            return null;
        }
        return value;
    }

    @Nullable
    public static MapObject getMapObject(long id) {
        MapObject value = (MapObject) getMapObjectsInternal().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Unknown Map Object: '" + id + "'");
            return null;
        }
        return value;
    }

    @Nullable
    public static Tileset getTileset(long id) {
        Tileset value = (Tileset) getTilesetsInternal().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Unknown Tileset: '" + id + "'");
            return null;
        }
        return value;
    }

    @Nullable
    public static DungeonMapTemplate getMapTemplate(long id) {
        DungeonMapTemplate value = (DungeonMapTemplate) getMapTemplatesInternal().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Unknown Map Template: '" + id + "'");
            return null;
        }
        return value;
    }

    @Nullable
    public static FastNoiseParameters getNoiseParameters(long id) {
        FastNoiseParameters value = (FastNoiseParameters) getNoiseParametersInternal().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Unknown Noise Parameters: '" + id + "'");
            return null;
        }
        return value;
    }

    @Nullable
    public static StatusEffect getStatusEffect(long id) {
        StatusEffect value = (StatusEffect) getStatusEffectsInternal().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Unknown Status Effect: '" + id + "'");
            return null;
        }
        return value;
    }

    @Nullable
    public static Item getItem(long id) {
        Item value = (Item) getItemsInternal().get(id);
        if (value == null) {
            if (DEBUG) LOGGER.error("Unknown Item: '" + id + "'");
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

                Object2ObjectArrayMap<String, Identifiable> data = STRING_ID_DATA.get(entry.getKey());
                Object2ObjectArrayMap<Long, Identifiable> internalData = INTERNAL_ID_DATA.get(entry.getKey());
                BiMap<Long, String> ids = IDS.get(entry.getKey());
                try (Stream<Path> paths = Files.walk(folderPath)) {
                    Stream<Path> filtered = paths.filter(p -> p.toString().endsWith(".json"));
                    long nextId = 0;
                    for (Path file : filtered.toList()) {
                        try (Reader reader = new FileReader(file.toFile())) {
                            JsonElement jsonValue = GSON.fromJson(reader, JsonElement.class);
                            DataResult<?> result = codec.parse(JsonOps.INSTANCE, jsonValue);

                            if (result.error().isPresent()) {
                                LOGGER.error("Error parsing " + file.getFileName() + " in '/" + folder + "': " + result.error().get().message());
                                continue;
                            }

                            String id = file.getFileName().toString();
                            id = id.substring(0, id.lastIndexOf('.'));

                            Identifiable obj = (Identifiable) result.getOrThrow();
                            if (obj.id().isEmpty())
                                obj = obj.withId(id);
                            data.put(obj.id(), obj);
                            ids.put(nextId, obj.id());
                            internalData.put(nextId++, obj);
                        } catch (Exception e) {
                            LOGGER.error("Error parsing " + file.getFileName() + " in '/" + folder + "': " + e);
                            e.printStackTrace();
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
        TILE_PROVIDER("tile_provider"),
        TILESET("tileset"),
        MAP_OBJECT("object"),
        MAP_TEMPLATE("map_template"),
        NOISE_PARAMETERS("noise_parameters"),
        STATUS_EFFECT("status_effect"),
        ITEM("item");

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
        SCHEMA.put(SchemaType.TILESET, Tileset.CODEC);
        SCHEMA.put(SchemaType.MAP_OBJECT, MapObject.CODEC);
        SCHEMA.put(SchemaType.MAP_TEMPLATE, DungeonMapTemplate.CODEC);
        SCHEMA.put(SchemaType.NOISE_PARAMETERS, FastNoiseParameters.CODEC);
        SCHEMA.put(SchemaType.STATUS_EFFECT, StatusEffect.CODEC);
        SCHEMA.put(SchemaType.ITEM, Item.CODEC);

        for (SchemaType schemaType : SCHEMA.keySet()) {
            STRING_ID_DATA.put(schemaType, new Object2ObjectArrayMap<>());
            INTERNAL_ID_DATA.put(schemaType, new Object2ObjectArrayMap<>());
            IDS.put(schemaType, HashBiMap.create());
        }
    }
}
