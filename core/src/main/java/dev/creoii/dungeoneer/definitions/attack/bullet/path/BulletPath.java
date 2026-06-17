package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.SidedBullet;
import dev.creoii.dungeoneer.util.Identifiable;

public sealed interface BulletPath extends Identifiable permits OrbitBulletPath, ParametricBulletPath, SegmentedBulletPath, StraightBulletPath, WavyBulletPath {
    BulletPathType type();

    default void start(SidedBullet bullet, float dt, BulletPath previous) {
        bullet.setStartPos(bullet.getPos().x, bullet.getPos().y);
        bullet.resetDistanceTravelled();
        bullet.setSegmentStartAge(bullet.getAge());
    }

    void apply(SidedBullet bullet, float dt);

    Codec<BulletPath> CODEC = BulletPathType.CODEC.dispatch(BulletPath::type, type -> switch (type) {
        case STRAIGHT -> StraightBulletPath.TYPE_CODEC;
        case WAVY -> WavyBulletPath.TYPE_CODEC;
        case ORBIT -> OrbitBulletPath.TYPE_CODEC;
        case SEGMENTED -> SegmentedBulletPath.TYPE_CODEC;
        case PARAMETRIC -> ParametricBulletPath.TYPE_CODEC;
    });

    static <T extends BulletPath> Products.P2<RecordCodecBuilder.Mu<T>, String, BulletPathType> addDefaultFields(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(Codec.STRING.fieldOf("id").forGetter(BulletPath::id), BulletPathType.CODEC.fieldOf("type").forGetter(BulletPath::type));
    }
}
