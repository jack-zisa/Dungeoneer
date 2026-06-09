package dev.creoii.dungeoneer.client;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AssetManager {
    public static final TextureRegion MARKET_TEXTURE = new TextureRegion(new Texture("textures/ui/market.png"));
    public static final TextureRegion THRONE_TEXTURE = new TextureRegion(new Texture("textures/ui/throne.png"));
    public static final TextureRegion CHEST_TEXTURE = new TextureRegion(new Texture("textures/ui/chest.png"));
    public static final TextureRegion TOWER_TEXTURE = new TextureRegion(new Texture("textures/ui/tower.png"));
    public static final TextureRegion SKULL_TEXTURE = new TextureRegion(new Texture("textures/ui/skull.png"));
    public static final TextureRegion TAB_ARROW_TEXTURE = new TextureRegion(new Texture("textures/ui/tab_arrow.png"));
    public static final TextureRegion GOLD_TEXTURE = new TextureRegion(new Texture("textures/ui/gold.png"));
    public static final TextureRegion GEM_TEXTURE = new TextureRegion(new Texture("textures/ui/gem.png"));
    public static final TextureRegion HEART_TEXTURE = new TextureRegion(new Texture("textures/ui/heart.png"));
    public static final TextureRegion HEART_DISABLED_TEXTURE = new TextureRegion(new Texture("textures/ui/heart_disabled.png"));

    public static final TextureRegion STONE_TEXTURE = new TextureRegion(new Texture("textures/tile/stone.png"));

    public static final TextureRegion BACKGROUND_BRICK_TEXTURE = new TextureRegion(new Texture("textures/ui/background_brick.png"));

    public static final TextureRegion WIZARD_TEXTURE = new TextureRegion(new Texture("textures/character/wizard.png"));
    public static final TextureRegion KNIGHT_TEXTURE = new TextureRegion(new Texture("textures/character/knight.png"));
    public static final TextureRegion ARCHER_TEXTURE = new TextureRegion(new Texture("textures/character/archer.png"));
    public static final TextureRegion ROGUE_TEXTURE = new TextureRegion(new Texture("textures/character/rogue.png"));
    public static final TextureRegion PRIEST_TEXTURE = new TextureRegion(new Texture("textures/character/priest.png"));
    public static final TextureRegion NINJA_TEXTURE = new TextureRegion(new Texture("textures/character/ninja.png"));
    public static final TextureRegion CLASS_SILHOUETTE_TEXTURE = new TextureRegion(new Texture("textures/character/silhouette.png"));

    public static final TextureRegion MISSING_TEXTURE = new TextureRegion(new Texture("textures/misc/missing.png"));

    public static final NinePatch TAB_9PATCH = new NinePatch(new Texture("textures/ui/tab.png"), 2, 2, 2,2);
    public static final NinePatch TAB_SELECTED_9PATCH = new NinePatch(new Texture("textures/ui/tab_selected.png"), 2, 2, 2,2);
    public static final NinePatch HEALTH_BAR_9PATCH = new NinePatch(new Texture("textures/ui/health_bar.png"), 4, 4, 4,4);
    public static final NinePatch HEALTH_BAR_EMPTY_9PATCH = new NinePatch(new Texture("textures/ui/health_bar_empty.png"), 4, 4, 4,4);

    public static TextureRegion getClassTexture(String classId) {
        return switch (classId) {
            case "wizard" -> AssetManager.WIZARD_TEXTURE;
            case "knight" -> AssetManager.KNIGHT_TEXTURE;
            case "ninja" -> AssetManager.NINJA_TEXTURE;
            case "priest" -> AssetManager.PRIEST_TEXTURE;
            case "rogue" -> AssetManager.ROGUE_TEXTURE;
            case "archer" -> AssetManager.ARCHER_TEXTURE;
            default -> AssetManager.CLASS_SILHOUETTE_TEXTURE;
        };
    }

    static {
        TAB_9PATCH.scale(4f, 4f);
        TAB_SELECTED_9PATCH.scale(4f, 4f);
    }
}
