package dev.creoii.dungeoneer.network.s2c.character;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.item.inventory.Slot;
import dev.creoii.dungeoneer.util.PacketUtils;

import java.util.ArrayList;
import java.util.List;

public record SyncEquipmentS2C(long accountId, long characterId, List<Slot> slots) {
    public static void write(Output output, SyncEquipmentS2C o) {
        output.writeLong(o.accountId);
        output.writeLong(o.characterId);
        output.writeInt(o.slots.size(), true);
        for (Slot slot : o.slots) {
            PacketUtils.writeSlot(output, slot);
        }
    }

    public static SyncEquipmentS2C read(Input input) {
        long accountId = input.readLong();
        long characterId = input.readLong();
        int size = input.readInt(true);
        List<Slot> slots = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            slots.add(PacketUtils.readSlot(input));
        }
        return new SyncEquipmentS2C(accountId, characterId, slots);
    }
}
