package dev.creoii.dungeoneer.client.render.screen.game;

import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.render.screen.AbstractScreen;
import dev.creoii.dungeoneer.client.render.screen.BorderedImage;
import dev.creoii.dungeoneer.client.render.screen.StatsDisplay;
import dev.creoii.dungeoneer.definitions.item.EquipmentItem;
import dev.creoii.dungeoneer.definitions.item.Item;

public class TooltipWidget extends Table {
    protected static final NinePatchDrawable TOOLTIP_BACKGROUND = new NinePatchDrawable(Assets.TAB_9PATCH);
    private final StatsDisplay statBonus;

    public TooltipWidget(Dungeoneer client, Item item) {
        Image image = new BorderedImage(client.getAssets().getTexture(Assets.Atlas.ITEM, item.id()));
        Container<Image> imageContainer = new Container<>(image);
        imageContainer.fill();
        add(imageContainer).size(48f, 48f).left();

        Label name = new Label(item.id(), AbstractScreen.SKIN);
        if (item instanceof EquipmentItem equipmentItem) {
            statBonus = new StatsDisplay(equipmentItem.statBonus(), null);
        } else statBonus = null;

        setBackground(TOOLTIP_BACKGROUND);

        add(name).padBottom(.8f).row();
        add(statBonus).colspan(2);
    }
}
