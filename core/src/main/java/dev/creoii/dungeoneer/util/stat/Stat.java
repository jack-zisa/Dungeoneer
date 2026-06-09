package dev.creoii.dungeoneer.util.stat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.UUID;

public class Stat {
    private final Type type;
    private int base;
    private final ObjectList<ModifierEntry> modifiers = new ObjectArrayList<>();

    public Stat(Type type, int base) {
        this.type = type;
        this.base = base;
    }

    public Stat(Type type) {
        this(type, 0);
    }

    public Stat(Type type, int base, ObjectList<ModifierEntry> modifiers) {
        this.type = type;
        this.base = base;
        modifiers.forEach(this::addModifier);
    }

    public Type type() {
        return type;
    }

    public int base() {
        return base;
    }

    public ObjectList<ModifierEntry> getModifiers() {
        return modifiers;
    }

    public void set(int value) {
        this.base = value;
    }

    public void addModifier(ModifierEntry modifierEntry) {
        modifiers.add(modifierEntry);
    }

    public void removeModifier(UUID uuid) {
        modifiers.removeIf(modifier -> modifier.uuid().equals(uuid));
    }

    public int value() {
        int result = base;
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
        SPEED
    }
}
