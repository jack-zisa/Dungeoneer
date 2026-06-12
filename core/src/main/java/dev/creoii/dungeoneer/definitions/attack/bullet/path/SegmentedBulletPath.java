package dev.creoii.dungeoneer.definitions.attack.bullet.path;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.sided.SidedBullet;

import java.util.Map;
import java.util.TreeMap;

public record SegmentedBulletPath(String id, BulletPathType type, TreeMap<Float, BulletPath> segments) implements BulletPath {
    public static final MapCodec<SegmentedBulletPath> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
            return BulletPath.addDefaultFields(instance).and(Codec.list(Segment.CODEC).fieldOf("segments").forGetter(segmentedBulletPath -> segmentedBulletPath.segments.entrySet().stream().map(entry -> new Segment(entry.getKey(), entry.getValue())).toList())
            ).apply(instance, (id, type, segments) -> {
                TreeMap<Float, BulletPath> map = new TreeMap<>();
                for (Segment segment : segments) {
                    map.put(segment.threshold(), segment.path());
                }
                return new SegmentedBulletPath(id, type, map);
            });
        }
    );

    @Override
    public void apply(SidedBullet bullet, float dt) {
        Map.Entry<Float, BulletPath> entry = segments.floorEntry(bullet.getAge());

        if (entry == null) {
            return;
        }

        float threshold = entry.getKey();
        BulletPath path = entry.getValue();

        if (threshold != bullet.getCurrentSegmentThreshold()) {
            BulletPath previous = null;
            if (bullet.getCurrentSegmentThreshold() != -1f) {
                previous = segments.get(bullet.getCurrentSegmentThreshold());
            }

            path.start(bullet, dt, previous);
            bullet.setCurrentSegmentThreshold(threshold);
        }

        path.apply(bullet, dt);
    }

    public record Segment(float threshold, BulletPath path) {
        public static final Codec<Segment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("threshold").forGetter(Segment::threshold),
            BulletPath.CODEC.fieldOf("path").forGetter(Segment::path)
        ).apply(instance, Segment::new));
    }
}
