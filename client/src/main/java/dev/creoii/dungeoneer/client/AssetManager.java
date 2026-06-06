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

    public static final TextureRegion WIZARD_TEXTURE = new TextureRegion(new Texture("textures/character/wizard.png"));
    public static final TextureRegion KNIGHT_TEXTURE = new TextureRegion(new Texture("textures/character/knight.png"));
    public static final TextureRegion ARCHER_TEXTURE = new TextureRegion(new Texture("textures/character/archer.png"));
    public static final TextureRegion ROGUE_TEXTURE = new TextureRegion(new Texture("textures/character/rogue.png"));
    public static final TextureRegion PRIEST_TEXTURE = new TextureRegion(new Texture("textures/character/priest.png"));
    public static final TextureRegion NINJA_TEXTURE = new TextureRegion(new Texture("textures/character/ninja.png"));

    public static final TextureRegion MISSING_TEXTURE = new TextureRegion(new Texture("textures/misc/missing.png"));

    public static final NinePatch TAB_9PATCH = new NinePatch(new Texture("textures/ui/tab.png"), 2, 2, 2 ,2);
    public static final NinePatch TAB_SELECTED_9PATCH = new NinePatch(new Texture("textures/ui/tab_selected.png"), 2, 2, 2 ,2);
}
