package dev.creoii.dungeoneer.definitions.attack;

import com.badlogic.gdx.math.Vector2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.util.Codecs;
import dev.creoii.dungeoneer.util.Identifiable;

public record BulletAttack(String id, int bulletCount, float arcGap, float angleOffset, Vector2 offset, int indexOffset) implements Attack {
    public static final MapCodec<BulletAttack> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return Attack.addDefaultFields(instance).and(instance.group(
            Codec.INT.fieldOf("bullet_count").orElse(1).forGetter(BulletAttack::bulletCount),
            Codec.FLOAT.fieldOf("arc_gap").orElse(0f).forGetter(BulletAttack::arcGap),
            Codec.FLOAT.fieldOf("angle_offset").orElse(0f).forGetter(BulletAttack::angleOffset),
            Codecs.VECTOR_2.fieldOf("offset").orElse(Vector2.Zero).forGetter(BulletAttack::offset),
            Codec.INT.fieldOf("index_offset").orElse(0).forGetter(BulletAttack::indexOffset)
        )).apply(instance, BulletAttack::new);
    });

    @Override
    public AttackType type() {
        return AttackType.BULLET;
    }

    @Override
    public Identifiable<String> withId(String id) {
        return new BulletAttack(id, bulletCount, arcGap, angleOffset, offset, indexOffset);
    }
}
