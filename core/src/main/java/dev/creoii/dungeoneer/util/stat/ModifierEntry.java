package dev.creoii.dungeoneer.util.stat;

import java.util.UUID;

public record ModifierEntry(Stat.Type type, UUID uuid, int amount, Operation operation, ModifierType modifierType) {
    public void apply(StatContainer statContainer) {
        switch (type) {
            case SPEED -> statContainer.speed().addModifier(this);
        }
    }

    public void remove(StatContainer statContainer) {
        switch (type) {
            case SPEED -> statContainer.speed().removeModifier(uuid);
        }
    }

    public enum ModifierType {
        BASE,
        MAX,
        ALL
    }

    public enum Operation {
        NONE(""),
        ADD("+"),
        MULTIPLY("x"),
        SET("=");

        private final String prefix;

        Operation(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
    }
}
