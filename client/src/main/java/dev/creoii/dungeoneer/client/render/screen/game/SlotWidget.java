package dev.creoii.dungeoneer.client.render.screen.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.definitions.item.Item;
import dev.creoii.dungeoneer.definitions.item.inventory.Slot;
import org.jspecify.annotations.Nullable;

public class SlotWidget extends Widget {
    private final Dungeoneer client;
    private final Slot slot;

    public SlotWidget(Dungeoneer client, Slot slot) {
        this.client = client;
        this.slot = slot;
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

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Texture slotTexture = slot.isEmpty() && slot.getSlotType() != null ? getSlotTexture(slot.getSlotType().getType()) : client.getAssets().getTexture(Assets.Atlas.UI, "slot/empty");

        batch.disableBlending();
        batch.draw(slotTexture, getX(), getY(), getWidth(), getHeight());
        batch.enableBlending();

        if (!slot.isEmpty()) {
            batch.setShader(Assets.BORDER_SHADER);

            Texture itemTexture = client.getAssets().getTexture(Assets.Atlas.ITEM, slot.getItem().id());

            Assets.BORDER_SHADER.setUniformf("u_pixelSize", (1f / itemTexture.getWidth()) * .25f, (1f / itemTexture.getHeight()) * .25f);
            Assets.BORDER_SHADER.setUniformf("u_borderColor", Color.BLACK);

            float offsetX = getWidth() * .1f;
            float offsetY = getHeight() * .1f;
            batch.draw(itemTexture, getX() + offsetX, getY() + offsetY, getWidth() * .8f, getHeight() * .8f);
            batch.setShader(null);
        }

        super.draw(batch, parentAlpha);
    }

    public Texture getSlotTexture(Item.@Nullable Type type) {
        return switch (type) {
            case WEAPON -> client.getAssets().getTexture(Assets.Atlas.UI, "slot/weapon");
            case ABILITY -> client.getAssets().getTexture(Assets.Atlas.UI, "slot/ability");
            case ARMOR -> client.getAssets().getTexture(Assets.Atlas.UI, "slot/armor");
            case ACCESSORY -> client.getAssets().getTexture(Assets.Atlas.UI, "slot/accessory");
            case CONSUMABLE -> client.getAssets().getTexture(Assets.Atlas.UI, "slot/consumable");
            default -> client.getAssets().getTexture(Assets.Atlas.UI, "slot/empty");
        };
    }
}
