package dev.creoii.dungeoneer.util.stat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.Optional;
import java.util.UUID;

public class Stat {
    public static final Codec<Stat> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Stat.Type.CODEC.fieldOf("stat_type").forGetter(Stat::type),
            Codec.FLOAT.optionalFieldOf("amount").forGetter(stat -> stat.base == 0f ? Optional.empty() : Optional.of(stat.base)),
            ModifierEntry.CODEC.listOf().optionalFieldOf("modifiers").forGetter(stat -> stat.modifiers.isEmpty() ? Optional.empty() : Optional.of(stat.modifiers))
        ).apply(instance, (type, amount, modifiers) -> modifiers.map(modifierEntries -> new Stat(type, amount.orElse(0f), new ObjectArrayList<>(modifierEntries))).orElseGet(() -> new Stat(type, amount.orElse(0f))));
    });
    private final Type type;
    private float base;
    private final ObjectList<ModifierEntry> modifiers = new ObjectArrayList<>();

    public Stat(Type type, float base) {
        this.type = type;
        this.base = base;
    }

    public Stat(Type type) {
        this(type, 0f);
    }

    public Stat(Type type, float base, ObjectList<ModifierEntry> modifiers) {
        this.type = type;
        this.base = base;
        modifiers.forEach(this::addModifier);
    }

    public Type type() {
        return type;
    }

    public float base() {
        return base;
    }

    public ObjectList<ModifierEntry> getModifiers() {
        return modifiers;
    }

    public void set(float value) {
        this.base = value;
    }

    public void addModifier(ModifierEntry modifierEntry) {
        modifiers.add(modifierEntry);
    }

    public void removeModifier(UUID uuid) {
        modifiers.removeIf(modifier -> modifier.uuid().equals(uuid));
    }

    public float value() {
        float result = base;
        for (ModifierEntry mod : modifiers) {
            switch (mod.operation()) {
                case ADD -> result += mod.amount();
                case SET -> result = mod.amount();
                case MULTIPLY -> result *= mod.amount();
            }
        }
        return Math.max(0, result);
    }

    @Override
    public String toString() {
        return String.valueOf(value());
    }

    public enum Type {
        HEALTH,
        SPEED,
        ATTACK_SPEED;

        public static final Codec<Type> CODEC = Codec.STRING.xmap(s -> Type.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    }
}
