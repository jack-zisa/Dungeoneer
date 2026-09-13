package dev.creoii.dungeoneer.util.stat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.*;

public class Stat {
    public static final Codec<Stat> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Stat.Type.CODEC.fieldOf("stat_type").forGetter(Stat::type),
            Codec.FLOAT.optionalFieldOf("amount").forGetter(stat -> stat.base == 0f ? Optional.empty() : Optional.of(stat.base)),
            ModifierEntry.CODEC.listOf().optionalFieldOf("modifiers").forGetter(stat -> stat.modifiers.isEmpty() ? Optional.empty() : Optional.of(new ArrayList<>(stat.modifiers.values())))
        ).apply(instance, (type, amount, modifiers) -> {
            Stat stat = new Stat(type, amount.orElse(0f));
            modifiers.ifPresent(entries -> entries.forEach(stat::addModifier));
            return stat;
        });
    });
    private final Type type;
    private float base;
    private final Map<UUID, ModifierEntry> modifiers = new HashMap<>();

    public Stat(Type type, float base) {
        this.type = type;
        this.base = base;
    }

    public Stat(Type type) {
        this(type, 0f);
    }

    public Stat(Type type, float base, Map<UUID, ModifierEntry> modifiers) {
        this.type = type;
        this.base = base;
        modifiers.values().forEach(this::addModifier);
    }

    public Type type() {
        return type;
    }

    public float base() {
        return base;
    }

    public Map<UUID, ModifierEntry> getModifiers() {
        return modifiers;
    }

    public void set(float value) {
        this.base = value;
    }

    public void addModifier(ModifierEntry modifierEntry) {
        modifiers.put(modifierEntry.uuid(), modifierEntry);
    }

    public void removeModifier(UUID uuid) {
        modifiers.remove(uuid);
    }

    public float value() {
        float result = base;
        for (ModifierEntry mod : modifiers.values()) {
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
        DEFENSE,
        SPEED,
        DEXTERITY,
        VITALITY;

        public static final Codec<Type> CODEC = Codec.STRING.xmap(s -> Type.valueOf(s.toUpperCase()), type -> type.name().toLowerCase());
    }
}
