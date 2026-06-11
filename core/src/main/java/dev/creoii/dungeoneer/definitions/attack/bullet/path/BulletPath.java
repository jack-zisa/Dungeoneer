package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.SidedBullet;
import dev.creoii.dungeoneer.util.Identifiable;

public interface BulletPath extends Identifiable {
    BulletPathType type();

    void apply(SidedBullet bullet, float dt);

    Codec<BulletPath> CODEC = BulletPathType.CODEC.dispatch(BulletPath::type, type -> switch (type) {
        case FORWARD -> ForwardBulletPath.TYPE_CODEC;
        case WAVY -> WavyBulletPath.TYPE_CODEC;
        case ORBIT -> OrbitBulletPath.TYPE_CODEC;
    });

    static <T extends BulletPath> Products.P2<RecordCodecBuilder.Mu<T>, String, BulletPathType> addDefaultFields(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(Codec.STRING.fieldOf("id").forGetter(BulletPath::id), BulletPathType.CODEC.fieldOf("type").forGetter(BulletPath::type));
    }
}
