package dev.creoii.dungeoneer.definitions.attack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BulletAttack(String id, AttackType type, int bulletCount, float arcGap, float angleOffset) implements Attack {
    public static final MapCodec<BulletAttack> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return Attack.addDefaultFields(instance).and(instance.group(
            Codec.INT.fieldOf("bullet_count").orElse(1).forGetter(BulletAttack::bulletCount),
            Codec.FLOAT.fieldOf("arc_gap").orElse(0f).forGetter(BulletAttack::arcGap),
            Codec.FLOAT.fieldOf("angle_offset").orElse(0f).forGetter(BulletAttack::angleOffset)
        )).apply(instance, BulletAttack::new);
    });
}
