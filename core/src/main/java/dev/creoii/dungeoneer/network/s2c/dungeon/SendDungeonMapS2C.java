package dev.creoii.dungeoneer.network.s2c.dungeon;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.creoii.dungeoneer.definitions.map.DungeonMapDefinition;
import dev.creoii.dungeoneer.util.PacketUtils;

public record SendDungeonMapS2C(DungeonMapDefinition dungeonMap) {
    public static void write(Output output, SendDungeonMapS2C o) {
        PacketUtils.writeDungeonMap(output, o.dungeonMap);
    }

    public static SendDungeonMapS2C read(Input input) {
        return new SendDungeonMapS2C(PacketUtils.readDungeonMap(input));
    }
}
