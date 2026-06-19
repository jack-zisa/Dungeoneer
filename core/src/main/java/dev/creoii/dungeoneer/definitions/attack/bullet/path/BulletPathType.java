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
        case PARAMETRIC -> ParametricBulletPathType.TYPE_CODEC;
    });
    BulletPathType<EmptyBulletPathType.EmptyBulletPathInstance> EMPTY = new EmptyBulletPathType("empty");

    Type type();

    T create();

    static <P extends BulletPathType<?>> Products.P1<RecordCodecBuilder.Mu<P>, String> addDefaultFields(RecordCodecBuilder.Instance<P> instance) {
        return instance.group(Codec.STRING.fieldOf("id").forGetter(BulletPathType::id));
    }

    abstract class Instance<T extends BulletPathType<?>> implements Pool.Poolable {
        public static final float[] ZERO = new float[]{0f, 0f};
        public static final float[] RIGHT = new float[]{0f, 1f};

        private final T type;

        public Instance(T type) {
            this.type = type;
        }

        public T getType() {
            return type;
        }

        public float[] getOffset(BulletNode<?> node, float t) {
            return ZERO;
        }

        public float[] getDirection(BulletNode<?> node, float[] offset1, float[] offset2) {
            float dx = offset2[0] - offset1[0];
            float dy = offset2[1] - offset1[1];

            float len = (float) Math.sqrt(dx * dx + dy * dy);

            if (len == 0f)
                return RIGHT;
            return new float[]{dx / len, dy / len};
        }

        @Override
        public void reset() {
        }
    }

    enum Type {
        EMPTY,
        STRAIGHT,
        WAVY,
        ORBIT,
        PARAMETRIC;

        public static final Codec<Type> CODEC = Codec.STRING.xmap(s -> Type.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    }
}
