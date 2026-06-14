package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.BulletNode;

import java.util.Map;
import java.util.TreeMap;

public record SegmentedBulletPathType(String id, TreeMap<Float, BulletPathType<?>> segments) implements BulletPathType<SegmentedBulletPathType.SegmentedBulletPathInstance> {
    public static final MapCodec<SegmentedBulletPathType> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
            return BulletPathType.addDefaultFields(instance).and(Codec.list(Segment.CODEC).fieldOf("segments").forGetter(segmentedBulletPath -> segmentedBulletPath.segments.entrySet().stream().map(entry -> new Segment(entry.getKey(), entry.getValue())).toList())
            ).apply(instance, (id, segments) -> {
                TreeMap<Float, BulletPathType<?>> map = new TreeMap<>();
                for (Segment segment : segments) {
                    map.put(segment.threshold(), segment.path());
                }
                return new SegmentedBulletPathType(id, map);
            });
        }
    );

    @Override
    public Type type() {
        return Type.SEGMENTED;
    }

    @Override
    public SegmentedBulletPathInstance create() {
        return new SegmentedBulletPathInstance(this);
    }

    public static class SegmentedBulletPathInstance extends Instance<SegmentedBulletPathType> {
        private final TreeMap<Float, Instance<?>> segments;
        private float segmentStartAge;
        private float currentSegmentThreshold;

        public SegmentedBulletPathInstance(SegmentedBulletPathType definition) {
            super(definition);
            segments = new TreeMap<>();
            definition.segments.forEach((aFloat, bulletPathType) -> segments.put(aFloat, bulletPathType.create()));
        }

        public float getCurrentSegmentThreshold() {
            return currentSegmentThreshold;
        }

        public float getSegmentStartAge() {
            return segmentStartAge;
        }

        public void setSegmentStartAge(float segmentStartAge) {
            this.segmentStartAge = segmentStartAge;
        }

        @Override
        public void start(BulletNode bullet, float dt, Instance<?> previous) {
            super.start(bullet, dt, previous);
        }

        @Override
        public void reset() {
            segments.clear();
            currentSegmentThreshold = 0f;
        }

        @Override
        public void update(BulletNode bullet, float dt) {
            Map.Entry<Float, Instance<?>> entry = segments.floorEntry(bullet.getAge());

            if (entry == null) {
                return;
            }

            float threshold = entry.getKey();
            Instance<?> path = entry.getValue();

            if (threshold != currentSegmentThreshold) {
                Instance<?> previous = null;
                if (currentSegmentThreshold != -1f) {
                    previous = segments.get(currentSegmentThreshold);
                }

                path.start(bullet, dt, previous);
                currentSegmentThreshold = threshold;
            }

            path.update(bullet, dt);
        }
    }

    public record Segment(float threshold, BulletPathType<?> path) {
        public static final Codec<Segment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("threshold").forGetter(Segment::threshold),
            BulletPathType.CODEC.fieldOf("path").orElse(BulletPathType.EMPTY).forGetter(Segment::path)
        ).apply(instance, Segment::new));
    }
}
