package dev.creoii.dungeoneer.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import dev.creoii.dungeoneer.client.util.ConditionalPaddedTextureLoader;
import dev.creoii.dungeoneer.client.util.DynamicTextureAtlas;
import dev.creoii.dungeoneer.util.logging.Logger;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class Assets implements Disposable {
    public static final Texture MISSING_TEXTURE = new Texture("textures/misc/missing.png");
    public static final TextureRegion MISSING_TEXTURE_REGION = new TextureRegion(MISSING_TEXTURE);

    public static final Texture PURPLE_LASER = new Texture("textures/laser/purple.png");
    public static final TextureRegion PURPLE_LASER_REGION = new TextureRegion(PURPLE_LASER);

    public static final NinePatch TAB_9PATCH = new NinePatch(new Texture("textures/ui/tab.png"), 2, 2, 2,2);
    public static final NinePatch TAB_SELECTED_9PATCH = new NinePatch(new Texture("textures/ui/tab_selected.png"), 2, 2, 2,2);
    public static final NinePatch HEALTH_BAR_9PATCH = new NinePatch(new Texture("textures/ui/health_bar.png"), 4, 4, 4,4);
    public static final NinePatch HEALTH_BAR_EMPTY_9PATCH = new NinePatch(new Texture("textures/ui/health_bar_empty.png"), 4, 4, 4,4);

    public static final ShaderProgram BORDER_SHADER = new ShaderProgram(Gdx.files.internal("shaders/border.vert"), Gdx.files.internal("shaders/border.frag"));

    public static final Logger LOGGER = new Logger(AssetManager.class.getSimpleName());
    private final AssetManager manager;
    private final Int2ObjectOpenHashMap<DynamicTextureAtlas> atlases;

    public Assets() {
        manager = new AssetManager();
        atlases = new Int2ObjectOpenHashMap<>();
    }

    public AssetManager getManager() {
        return manager;
    }

    public Texture getTexture(Atlas atlas, String texture) {
        if (!atlases.containsKey(atlas.ordinal())) {
            LOGGER.warn("Unknown atlas requested from: %s", atlas.name());
            return atlas == Atlas.CHARACTER ? getTexture(atlas, "silhouette") : MISSING_TEXTURE;
        }
        return atlases.get(atlas.ordinal()).getTexture(texture, MISSING_TEXTURE_REGION).getTexture();
    }

    public void load() {
        FileHandle baseDir = Gdx.files.internal("textures");

        if (!baseDir.exists()) {
            LOGGER.info("Directory 'textures/' does not exist.");
            return;
        }

        manager.setLoader(Texture.class, new ConditionalPaddedTextureLoader(new InternalFileHandleResolver()));

        FileHandle assets = Gdx.files.internal("assets.txt");

        for (String line : assets.readString().split("\\R")) {
            if (!line.startsWith("textures/") || !line.endsWith(".png"))
                continue;

            String path = line.trim();
            String[] parts = path.split("/");
            if (parts.length < 3)
                continue;

            try {
                Atlas atlas = Atlas.valueOf(parts[1].toUpperCase());

                TextureLoader.TextureParameter params = null;

                if (atlas.hasOutline()) {
                    params = new ConditionalPaddedTextureLoader.PaddedTextureParameter(1);
                }

                manager.load(path, Texture.class, params);

                if (!atlases.containsKey(atlas.ordinal())) {
                    atlases.put(atlas.ordinal(), new DynamicTextureAtlas());
                }

                atlases.get(atlas.ordinal()).addTexture(path, parts[2].replace(".png", ""));
            } catch (IllegalArgumentException _) {

            }
        }
    }

    public void bindAtlases() {
        for (int key : atlases.keySet()) {
            Atlas atlas = Atlas.values()[key];
            if (atlas != null) {
                DynamicTextureAtlas dynamicTextureAtlas = atlases.get(key);
                LOGGER.info("Created dynamic atlas '" + atlas.name().toLowerCase() + "' size: " + dynamicTextureAtlas.getPendingTextures().size);
                dynamicTextureAtlas.bindTextures(manager);
            }
        }
    }

    @Override
    public void dispose() {
        for (DynamicTextureAtlas atlas : atlases.values()) {
            atlas.dispose();
        }
    }

    public enum Atlas {
        CHARACTER(true),
        BULLET(true),
        TILE,
        UI;

        private final boolean hasOutline;

        Atlas(boolean hasOutline) {
            this.hasOutline = hasOutline;
        }

        Atlas() {
            this(false);
        }

        public boolean hasOutline() {
            return hasOutline;
        }
    }

    static {
        TAB_9PATCH.scale(4f, 4f);
        TAB_SELECTED_9PATCH.scale(4f, 4f);
    }
}
