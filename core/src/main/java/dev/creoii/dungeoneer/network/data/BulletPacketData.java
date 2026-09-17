package dev.creoii.dungeoneer.network.data;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record BulletPacketData(int damage, float dirX, float dirY, String bulletType, int index, boolean enemy) implements EntityPacketData {
    @Override
    public Type type() {
        return Type.BULLET;
    }

    @Override
    public void write(Output output) {
        output.writeInt(damage);
        output.writeFloat(dirX);
        output.writeFloat(dirY);
        output.writeString(bulletType);
        output.writeInt(index);
        output.writeBoolean(enemy);
    }

    public static BulletPacketData read(Input input) {
        return new BulletPacketData(input.readInt(), input.readFloat(), input.readFloat(), input.readString(), input.readInt(), input.readBoolean());
    }
}
