package dev.creoii.dungeoneer.util.stat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.util.Codecs;

import java.util.UUID;

public record ModifierEntry(Stat.Type type, UUID uuid, float amount, Operation operation, ModifierType modifierType) {
    public static final Codec<ModifierEntry> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Stat.Type.CODEC.fieldOf("type").forGetter(ModifierEntry::type),
            Codecs.UUID.fieldOf("uuid").orElse(UUID.randomUUID()).forGetter(ModifierEntry::uuid),
            Codec.FLOAT.fieldOf("amount").orElse(0f).forGetter(ModifierEntry::amount),
            Operation.CODEC.fieldOf("operation").orElse(Operation.NONE).forGetter(ModifierEntry::operation),
            ModifierType.CODEC.fieldOf("modifier_type").orElse(ModifierType.ALL).forGetter(ModifierEntry::modifierType)
        ).apply(instance, ModifierEntry::new);
    });

    public void apply(StatContainer statContainer) {
        switch (type) {
            case HEALTH -> statContainer.health().addModifier(this);
            case SPEED -> statContainer.speed().addModifier(this);
            case DEXTERITY -> statContainer.dexterity().addModifier(this);
        }
    }

    public void remove(StatContainer statContainer) {
        switch (type) {
            case HEALTH -> statContainer.health().removeModifier(uuid);
            case SPEED -> statContainer.speed().removeModifier(uuid);
            case DEXTERITY -> statContainer.dexterity().removeModifier(uuid);
        }
    }

    public enum ModifierType {
        BASE,
        MAX,
        ALL;

        public static final Codec<ModifierType> CODEC = Codec.STRING.xmap(s -> ModifierType.valueOf(s.toUpperCase()), modifierType -> modifierType.name().toLowerCase());
    }

    public enum Operation {
        NONE(""),
        ADD("+"),
        MULTIPLY("x"),
        SET("=");

        public static final Codec<Operation> CODEC = Codec.STRING.xmap(s -> Operation.valueOf(s.toUpperCase()), operation -> operation.name().toLowerCase());
        private final String prefix;

        Operation(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
    }
}
