package dev.creoii.dungeoneer.network.data;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public record BulletGroupPacketData(int damage, float dirX, float dirY, String bulletType, int index, boolean enemy, long[] children) implements EntityPacketData {
    @Override
    public Type type() {
        return Type.BULLET_GROUP;
    }

    @Override
    public void write(Output output) {
        output.writeInt(damage);
        output.writeFloat(dirX);
        output.writeFloat(dirY);
        output.writeString(bulletType);
        output.writeInt(index);
        output.writeBoolean(enemy);

        int size = children.length;
        output.writeInt(size, true);
        for (int i = 0; i < size; ++i) {
            output.writeLong(children[i]);
        }
    }

    public static BulletGroupPacketData read(Input input) {
        int damage = input.readInt();
        float dirX = input.readFloat();
        float dirY = input.readFloat();
        String type = input.readString();
        int index = input.readInt();
        boolean enemy = input.readBoolean();

        int size = input.readInt(true);
        long[] children = new long[size];
        for (int i = 0; i < size; ++i) {
            children[i] = input.readLong();
        }
        return new BulletGroupPacketData(damage, dirX, dirY, type, index, enemy, children);
    }
}
