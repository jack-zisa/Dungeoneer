package dev.creoii.dungeoneer.util.action;

import com.mojang.serialization.MapCodec;
import dev.creoii.dungeoneer.definitions.sided.Raid;
import dev.creoii.dungeoneer.util.Identifiable;

public record EmptyAction(String id) implements Action {
    public static final EmptyAction INSTANCE = new EmptyAction("empty");
    public static final MapCodec<EmptyAction> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public void apply(Raid<?, ?, ?, ?> raid, Context context) {
    }

    @Override
    public Type getType() {
        return Type.EMPTY;
    }

    @Override
    public Identifiable withId(String id) {
        return this;
    }
}
