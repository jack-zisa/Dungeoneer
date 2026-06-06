package dev.creoii.dungeoneer.client.screen.main;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import dev.creoii.dungeoneer.client.Dungeoneer;

public class ShopTab extends Tab {
    protected ShopTab(Dungeoneer client, TextureRegion tabTexture) {
        super(client, tabTexture);
    }

    @Override
    protected void build() {
        add(new Label("Shop", getSkin()));
    }
}
