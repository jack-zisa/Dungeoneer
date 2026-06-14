package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.badlogic.gdx.math.MathUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;

public record WavyBulletPathType(String id, WaveType waveType, float amplitude, float frequency, boolean indexPhase) implements BulletPathType<WavyBulletPathType.WavyBulletPathInstance> {
    public static final MapCodec<WavyBulletPathType> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return BulletPathType.addDefaultFields(instance).and(instance.group(
            WaveType.CODEC.fieldOf("wave_type").orElse(WaveType.SIN).forGetter(WavyBulletPathType::waveType),
            Codec.FLOAT.fieldOf("amplitude").orElse(0f).forGetter(WavyBulletPathType::amplitude),
            Codec.FLOAT.fieldOf("frequency").orElse(0f).forGetter(WavyBulletPathType::frequency),
            Codec.BOOL.fieldOf("index_phase").orElse(false).forGetter(WavyBulletPathType::indexPhase)
        )).apply(instance, WavyBulletPathType::new);
    });

    @Override
    public Type type() {
        return Type.WAVY;
    }

    @Override
    public WavyBulletPathInstance create() {
        return new WavyBulletPathInstance(this);
    }

    public static class WavyBulletPathInstance extends Instance<WavyBulletPathType> implements AngledBulletPath {
        private float angle;

        public WavyBulletPathInstance(WavyBulletPathType definition) {
            super(definition);
        }

        @Override
        public float getAngle() {
            return angle;
        }

        @Override
        public void incrementAngle(float f) {
            angle += f;
        }

        @Override
        public void reset() {
            angle = 0f;
        }

        @Override
        public float[] getOffset(BulletNode bullet, float dt) {
            float age = bullet.getAge();

            float phase = getType().indexPhase
                ? 0f
                : ((bullet.getIndex() & 1) == 0 ? 0f : .5f);

            float cycle = age * getType().frequency + phase;

            float wave = switch (getType().waveType) {
                case SIN -> MathUtils.sin(cycle * MathUtils.PI2) * getType().amplitude;

                case TRIANGLE -> {
                    float triangle =
                        2f * Math.abs(2f * (cycle - (float)Math.floor(cycle + .5f))) - 1f;
                    yield triangle * getType().amplitude;
                }

                case SQUARE ->
                    (MathUtils.sin(cycle * MathUtils.PI2) >= 0f ? 1f : -1f)
                        * getType().amplitude;
            };

            return new float[] {
                wave,
                bullet.getDistanceTravelled()
            };
        }
    }

    public enum WaveType {
        SIN,
        TRIANGLE,
        SQUARE;

        public static final Codec<WaveType> CODEC = Codec.STRING.xmap(s -> WaveType.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    }
}
