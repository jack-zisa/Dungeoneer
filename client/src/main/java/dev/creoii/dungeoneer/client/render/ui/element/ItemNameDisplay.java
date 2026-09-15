package dev.creoii.dungeoneer.client.render.ui.element;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.render.ui.screen.AbstractScreen;
import dev.creoii.dungeoneer.definitions.item.EquipmentItem;
import dev.creoii.dungeoneer.definitions.item.Item;

public class ItemNameDisplay extends Stack {
    public ItemNameDisplay(Item item) {
        Label name = new Label(item.displayName(), AbstractScreen.SKIN);
        name.setFontScale(1.25f);

        Image background;
        if (item instanceof EquipmentItem equipmentItem) {
            background = new Image(switch (equipmentItem.rarity()) {
                case COMMON -> Assets.COMMON_BACKGROUND_9PATCH;
                case UNCOMMON -> Assets.UNCOMMON_BACKGROUND_9PATCH;
                case RARE -> Assets.RARE_BACKGROUND_9PATCH;
                case LEGENDARY -> Assets.LEGENDARY_BACKGROUND_9PATCH;
                case MYTHICAL -> {
                    name.setColor(new Color(0x280000ff));
                    yield Assets.MYTHICAL_BACKGROUND_9PATCH;
                }
            });
        } else background = new Image(Assets.COMMON_BACKGROUND_9PATCH);

        Container<Image> imageContainer = new Container<>(background);
        imageContainer.fill(1.25f, .75f);

        add(imageContainer);
        add(name);
    }
}
