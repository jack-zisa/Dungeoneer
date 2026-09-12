package dev.creoii.dungeoneer.network.s2c.character;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.inventory.EquipmentInventory;
import dev.creoii.dungeoneer.definitions.inventory.Inventory;
import dev.creoii.dungeoneer.util.PacketUtils;

public record SyncEquipmentS2C(long accountId, EquipmentInventory equipment) {
    public static void write(Output output, SyncEquipmentS2C o) {
        output.writeLong(o.accountId);
        PacketUtils.writeInventory(output, o.equipment);
    }

    public static SyncEquipmentS2C read(Input input) {
        long accountId = input.readLong();
        Inventory inventory = PacketUtils.readInventory(input);
        return new SyncEquipmentS2C(accountId, (EquipmentInventory) inventory);
    }
}
