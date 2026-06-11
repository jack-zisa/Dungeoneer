package dev.creoii.dungeoneer.definitions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.util.Identifiable;

public record Attack(String id, int bulletCount, float arcGap, float angleOffset) implements Identifiable {
    public static final Codec<Attack> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.STRING.fieldOf("id").forGetter(Attack::id),
            Codec.INT.fieldOf("bullet_count").orElse(1).forGetter(Attack::bulletCount),
            Codec.FLOAT.fieldOf("arc_gap").orElse(0f).forGetter(Attack::arcGap),
            Codec.FLOAT.fieldOf("angle_offset").orElse(0f).forGetter(Attack::angleOffset)
        ).apply(instance, Attack::new);
    });
    public static final Codec<Attack> ID_CODEC = Codec.STRING.xmap(DataManager::getAttack, Attack::id);

    @Override
    public String toString() {
        return id;
    }
}
