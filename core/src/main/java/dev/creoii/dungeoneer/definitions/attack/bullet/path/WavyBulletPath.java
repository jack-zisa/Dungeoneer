package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.badlogic.gdx.math.MathUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.SidedBullet;

public record WavyBulletPath(String id, BulletPathType type, WaveType waveType, float amplitude, float frequency, boolean indexPhase) implements BulletPath {
    public static final MapCodec<WavyBulletPath> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return BulletPath.addDefaultFields(instance).and(instance.group(
            WaveType.CODEC.fieldOf("wave_type").orElse(WaveType.SIN).forGetter(WavyBulletPath::waveType),
            Codec.FLOAT.fieldOf("amplitude").orElse(0f).forGetter(WavyBulletPath::amplitude),
            Codec.FLOAT.fieldOf("frequency").orElse(0f).forGetter(WavyBulletPath::frequency),
            Codec.BOOL.fieldOf("index_phase").orElse(false).forGetter(WavyBulletPath::indexPhase)
        )).apply(instance, WavyBulletPath::new);
    });

    @Override
    public void apply(SidedBullet bullet, float dt) {
        float segmentAge = bullet.getAge() - bullet.getSegmentStartAge();

        float phase = indexPhase ? 0f : (bullet.getIndex() & 1) == 0 ? 0f : .5f;
        float cycle = segmentAge * frequency + phase;

        float wave = switch (waveType) {
            case SIN -> MathUtils.sin(cycle * MathUtils.PI2) * amplitude;
            case TRIANGLE -> {
                float triangle = 2f * Math.abs(2f * (cycle - (float) Math.floor(cycle + .5f))) - 1f;
                yield triangle * amplitude;
            }
            case SQUARE -> (MathUtils.sin(cycle * MathUtils.PI2) >= 0f ? 1f : -1f) * amplitude;
        };

        bullet.getPos().set(
            bullet.getStartX() + bullet.getDirection().x * bullet.getDistanceTravelled() + -bullet.getDirection().y * wave,
            bullet.getStartY() + bullet.getDirection().y * bullet.getDistanceTravelled() + bullet.getDirection().x * wave
        );
    }

    public enum WaveType {
        SIN,
        TRIANGLE,
        SQUARE;

        public static final Codec<WaveType> CODEC = Codec.STRING.xmap(s -> WaveType.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    }
}
