package dev.creoii.dungeoneer.client.render.screen.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.Align;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.render.screen.BorderedImage;
import dev.creoii.dungeoneer.definitions.item.Item;
import dev.creoii.dungeoneer.definitions.item.inventory.Slot;
import org.jspecify.annotations.Nullable;

public class SlotWidget extends Container<Stack> {
    private final Dungeoneer client;
    private final Slot slot;
    private final Stack stack;
    private Container<Image> itemImage;

    public SlotWidget(Dungeoneer client, Slot slot) {
        this.client = client;
        this.slot = slot;
        setActor(stack = new Stack());

        Texture slotTexture = slot.isEmpty() && slot.getSlotType() != null ? getSlotTexture(slot.getSlotType().getType()) : client.getAssets().getTexture(Assets.Atlas.UI, "slot/empty");
        stack.add(new BorderedImage(slotTexture));

        fill();
        refresh();
    }

    public Slot getSlot() {
        return slot;
    }

    @Override
    public float getPrefWidth() {
        return 64f;
    }

    @Override
    public float getPrefHeight() {
        return 64f;
    }

    public void refresh() {
        if (itemImage != null) {
            itemImage.remove();
            itemImage = null;
        }

        if (!slot.isEmpty()) {
            Image image = new BorderedImage(client.getAssets().getTexture(Assets.Atlas.ITEM, slot.getItem().id()));
            itemImage = new Container<>(image).fill(.8f, .8f).align(Align.center);
            stack.add(itemImage);
        } else itemImage = null;

        invalidateHierarchy();
    }

    public Texture getSlotTexture(Item.@Nullable Type type) {
        return switch (type) {
            case WEAPON -> client.getAssets().getTexture(Assets.Atlas.UI, "slot/weapon");
            case ABILITY -> client.getAssets().getTexture(Assets.Atlas.UI, "slot/ability");
            case ARMOR -> client.getAssets().getTexture(Assets.Atlas.UI, "slot/armor");
            case ACCESSORY -> client.getAssets().getTexture(Assets.Atlas.UI, "slot/accessory");
            case CONSUMABLE -> client.getAssets().getTexture(Assets.Atlas.UI, "slot/consumable");
            case null -> client.getAssets().getTexture(Assets.Atlas.UI, "slot/empty");
        };
    }
}
