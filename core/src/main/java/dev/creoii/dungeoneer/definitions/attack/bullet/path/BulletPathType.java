package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.badlogic.gdx.utils.Pool;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;
import dev.creoii.dungeoneer.util.Identifiable;

public interface BulletPathType<T extends BulletPathType.Instance<?>> extends Identifiable {
    Codec<BulletPathType<?>> CODEC = Type.CODEC.dispatch(BulletPathType::type, type -> switch (type) {
        case EMPTY -> EmptyBulletPathType.TYPE_CODEC;
        case STRAIGHT -> StraightBulletPathType.TYPE_CODEC;
        case WAVY -> WavyBulletPathType.TYPE_CODEC;
        case ORBIT -> OrbitBulletPathType.TYPE_CODEC;
        case SEGMENTED -> SegmentedBulletPathType.TYPE_CODEC;
        case PARAMETRIC -> ParametricBulletPathType.TYPE_CODEC;
    });
    BulletPathType<EmptyBulletPathType.EmptyBulletPathInstance> EMPTY = new EmptyBulletPathType("empty");

    Type type();

    T create();

    static <P extends BulletPathType<?>> Products.P1<RecordCodecBuilder.Mu<P>, String> addDefaultFields(RecordCodecBuilder.Instance<P> instance) {
        return instance.group(Codec.STRING.fieldOf("id").forGetter(BulletPathType::id));
    }

    abstract class Instance<T extends BulletPathType<?>> implements Pool.Poolable {
        private final T type;

        public Instance(T type) {
            this.type = type;
        }

        public T getType() {
            return type;
        }

        public void start(BulletNode bullet, float dt, Instance<?> previous) {
            bullet.setStartPos(bullet.getX(), bullet.getY());
            bullet.resetDistanceTravelled();

            if (bullet.getPath() instanceof SegmentedBulletPathType.SegmentedBulletPathInstance instance)
                instance.setSegmentStartAge(bullet.getAge());
        }

        public abstract void update(BulletNode bullet, float dt);
    }

    enum Type {
        EMPTY,
        STRAIGHT,
        WAVY,
        ORBIT,
        SEGMENTED,
        PARAMETRIC;

        public static final Codec<Type> CODEC = Codec.STRING.xmap(s -> Type.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    }
}
