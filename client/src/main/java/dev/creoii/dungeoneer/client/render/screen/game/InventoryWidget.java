package dev.creoii.dungeoneer.client.render.screen.game;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.definitions.item.inventory.Inventory;
import dev.creoii.dungeoneer.definitions.item.inventory.Slot;

public class InventoryWidget extends Table {
    private final Dungeoneer client;

    public InventoryWidget(Dungeoneer client, Inventory inventory, int columns) {
        this.client = client;
        for (int i = 0; i < inventory.size(); ++i) {
            SlotWidget slotWidget = new SlotWidget(client, inventory.getSlot(i));
            add(slotWidget);
            if (i % columns == columns - 1) {
                padRight(8f);
                row();
            }
        }

        padBottom(8f);

        pack();
    }

    public Dungeoneer getClient() {
        return client;
    }

    public void refresh(Inventory inventory) {
        for (int i = 0; i < getChildren().size; ++i) {
            SlotWidget slotWidget = (SlotWidget) getChild(i);
            Slot slot = inventory.getSlot(i);
            if (slot == null)
                continue;
            slotWidget.getSlot().setSlotType(slot.getSlotType());
            slotWidget.getSlot().setItem(slot.getItem(), slot.getCount());
        }
        invalidate();
    }
}
