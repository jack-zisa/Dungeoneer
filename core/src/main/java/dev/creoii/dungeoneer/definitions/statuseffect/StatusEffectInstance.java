package dev.creoii.dungeoneer.definitions.statuseffect;

public record StatusEffectInstance(StatusEffect statusEffect, int amplifier, int duration, long startTime) {
    public boolean isExpired() {
        if (duration <= 0) return false;
        return System.currentTimeMillis() - startTime >= duration;
    }
}
