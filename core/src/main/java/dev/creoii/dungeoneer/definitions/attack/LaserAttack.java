package dev.creoii.dungeoneer.definitions.attack;

import com.badlogic.gdx.math.Vector2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.util.Codecs;
import dev.creoii.dungeoneer.util.Identifiable;

public record LaserAttack(String id, Vector2 size, int laserCount, float arcGap, float angleOffset, float lifetime, boolean attached) implements Attack {
    public static final MapCodec<LaserAttack> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return Attack.addDefaultFields(instance).and(instance.group(
            Codecs.VECTOR_2.fieldOf("size").orElse(Vector2.One.cpy()).forGetter(LaserAttack::size),
            Codec.INT.fieldOf("laser_count").orElse(1).forGetter(LaserAttack::laserCount),
            Codec.FLOAT.fieldOf("arc_gap").orElse(0f).forGetter(LaserAttack::arcGap),
            Codec.FLOAT.fieldOf("angle_offset").orElse(0f).forGetter(LaserAttack::angleOffset),
            Codec.FLOAT.fieldOf("lifetime").orElse(0f).forGetter(LaserAttack::lifetime),
            Codec.BOOL.fieldOf("attached").orElse(true).forGetter(LaserAttack::attached)
        )).apply(instance, LaserAttack::new);
    });

    @Override
    public AttackType type() {
        return AttackType.LASER;
    }

    @Override
    public Identifiable withId(String id) {
        return new LaserAttack(id, size, laserCount, arcGap, angleOffset, lifetime, attached);
    }
}
