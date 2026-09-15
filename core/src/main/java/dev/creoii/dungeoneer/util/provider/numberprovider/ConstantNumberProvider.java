package dev.creoii.dungeoneer.util.provider.numberprovider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.util.context.Context;
import dev.creoii.dungeoneer.util.Identifiable;

public record ConstantNumberProvider(String id, float value) implements NumberProvider {
    public static final ConstantNumberProvider ZERO = new ConstantNumberProvider("empty", 0);
    public static final ConstantNumberProvider ONE = new ConstantNumberProvider("empty", 1);
    public static final MapCodec<ConstantNumberProvider> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(ConstantNumberProvider::id),
            Codec.FLOAT.fieldOf("value").forGetter(ConstantNumberProvider::value)
        ).apply(instance, ConstantNumberProvider::new)
    );

    @Override
    public Type getType() {
        return Type.CONSTANT;
    }

    @Override
    public Number get(Context context) {
        return value;
    }

    @Override
    public Identifiable withId(String id) {
        return new ConstantNumberProvider(id, value);
    }

    @Override
    public String toString() {
        return String.format("%,d", (int) value);
    }
}
