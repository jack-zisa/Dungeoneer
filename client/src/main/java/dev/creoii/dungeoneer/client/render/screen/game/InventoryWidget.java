package dev.creoii.dungeoneer.client.render.screen.game;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.definitions.item.inventory.Inventory;
import dev.creoii.dungeoneer.definitions.item.inventory.Slot;

public class InventoryWidget extends Table {
    private final Dungeoneer client;
    private final SlotWidget[] slots;

    public InventoryWidget(Dungeoneer client, Inventory inventory, int columns) {
        this.client = client;
        this.slots = new SlotWidget[inventory.size()];

        for (int i = 0; i < inventory.size(); i++) {
            SlotWidget slotWidget = slots[i] = new SlotWidget(client, inventory.getSlot(i));

            add(slotWidget).size(64f);

            if (i % columns != columns - 1) {
                getCell(slotWidget);
            } else row();
        }
    }

    public Dungeoneer getClient() {
        return client;
    }

    public void refresh(Inventory inventory) {
        for (int i = 0; i < slots.length; i++) {
            SlotWidget slotWidget = slots[i];
            Slot slot = inventory.getSlot(i);
            slotWidget.getSlot().setSlotType(slot.getSlotType());
            slotWidget.getSlot().setItem(slot.getItem(), slot.getCount());
            slotWidget.refresh();
        }
        invalidateHierarchy();
    }
}
