package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.SidedBullet;
import dev.creoii.dungeoneer.util.Identifiable;

public interface BulletPath extends Identifiable {
    Codec<BulletPath> CODEC = BulletPathType.CODEC.dispatch(BulletPath::type, type -> switch (type) {
        case EMPTY -> EmptyBulletPath.TYPE_CODEC;
        case STRAIGHT -> StraightBulletPath.TYPE_CODEC;
        case WAVY -> WavyBulletPath.TYPE_CODEC;
        case ORBIT -> OrbitBulletPath.TYPE_CODEC;
        case SEGMENTED -> SegmentedBulletPath.TYPE_CODEC;
        case PARAMETRIC -> ParametricBulletPath.TYPE_CODEC;
    });
    BulletPath EMPTY = new EmptyBulletPath("empty");

    BulletPathType type();

    default void start(SidedBullet bullet, float dt, BulletPath previous) {
        bullet.setStartPos(bullet.getPos().x, bullet.getPos().y);
        bullet.resetDistanceTravelled();
        bullet.setSegmentStartAge(bullet.getAge());
    }

    void apply(SidedBullet bullet, float dt);

    static <T extends BulletPath> Products.P1<RecordCodecBuilder.Mu<T>, String> addDefaultFields(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(Codec.STRING.fieldOf("id").forGetter(BulletPath::id));
    }
}
