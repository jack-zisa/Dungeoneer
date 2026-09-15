package dev.creoii.dungeoneer.util.provider.numberprovider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.util.context.Context;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.action.value.ValueType;

import java.util.Random;

public record RandomNumberProvider(String id, float min, float max) implements NumberProvider {
    public static final MapCodec<RandomNumberProvider> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(RandomNumberProvider::id),
            Codec.FLOAT.fieldOf("min").forGetter(RandomNumberProvider::min),
            Codec.FLOAT.fieldOf("max").forGetter(RandomNumberProvider::max)
        ).apply(instance, RandomNumberProvider::new)
    );

    @Override
    public Type getType() {
        return Type.CONSTANT;
    }

    @Override
    public Number get(Context context) {
        if (context.has(ValueType.RANDOM)) {
            Random random = context.get(ValueType.RANDOM);
            return random.nextFloat(max) - min;
        }
        throw new IllegalStateException("Cannot call get() on a RandomNumberProvider with no random context.");
    }

    @Override
    public Identifiable withId(String id) {
        return new RandomNumberProvider(id, min, max);
    }

    @Override
    public String toString() {
        return String.format("%,d-%,d", (int) min, (int) max);
    }
}
