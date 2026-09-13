package dev.creoii.dungeoneer.util.provider.numberprovider;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.provider.Provider;

import java.util.function.Function;

public interface NumberProvider extends Provider<Number>, Identifiable {
    Codec<NumberProvider> TYPE_CODEC = Type.CODEC.dispatch(NumberProvider::getType, type -> switch (type) {
        case CONSTANT -> ConstantNumberProvider.CODEC;
        case RANDOM -> RandomNumberProvider.CODEC;
    });
    Codec<NumberProvider> CODEC = Codec.either(Codec.FLOAT, TYPE_CODEC).xmap(either -> {
        return either.map(aFloat -> new ConstantNumberProvider("", aFloat), Function.identity());
    }, Either::right);

    Type getType();

    enum Type {
        CONSTANT,
        RANDOM;

        public static final Codec<Type> CODEC = Codec.STRING.xmap(s -> Type.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    }
}
