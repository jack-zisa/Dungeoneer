package dev.creoii.dungeoneer.client.render.ui.element;

import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.render.ui.screen.AbstractScreen;
import dev.creoii.dungeoneer.definitions.item.EquipmentItem;
import dev.creoii.dungeoneer.definitions.item.Item;
import dev.creoii.dungeoneer.definitions.item.WeaponItem;

public class TooltipWidget extends Table {
    protected static final NinePatchDrawable TOOLTIP_BACKGROUND = new NinePatchDrawable(Assets.TAB_9PATCH);

    public TooltipWidget(Dungeoneer client, Item item) {
        Image image = new BorderedImage(client.getAssets().getTexture(Assets.Atlas.ITEM, item.id()));
        Container<Image> imageContainer = new Container<>(image);
        imageContainer.fill();
        add(imageContainer).size(48f, 48f).left();

        ItemNameDisplay name = new ItemNameDisplay(item);

        Label description;
        if (!item.description().isBlank()) {
            description = new Label(item.description(), AbstractScreen.SKIN);
            description.setWrap(true);
        } else description = null;

        Label damage;
        if (item instanceof WeaponItem weaponItem) {
            damage = new Label("Damage: " + weaponItem.damage().toString(), AbstractScreen.SKIN);
            damage.setFontScale(1.5f);
        } else damage = null;

        StatsDisplay statBonus;
        if (item instanceof EquipmentItem equipmentItem) {
            statBonus = new StatsDisplay(equipmentItem.statBonus(), null);
        } else statBonus = null;

        setBackground(TOOLTIP_BACKGROUND);

        add(name).padBottom(.8f).padTop(8f).row();
        if (description != null) add(description).padBottom(.8f).padTop(8f).fillX().colspan(2).row();
        if (damage != null) add(damage).padBottom(8f).padTop(8f).colspan(2).row();
        if (statBonus != null) add(statBonus).padBottom(8f).padTop(8f).colspan(2);

        if (client.getSettings().debug().value()) {
            row();
            Label rawId = new Label(item.id(), AbstractScreen.SKIN);
            add(rawId).left().colspan(1);
        }
    }
}
