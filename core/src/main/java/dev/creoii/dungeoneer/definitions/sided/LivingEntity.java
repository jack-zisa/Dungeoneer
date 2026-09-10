package dev.creoii.dungeoneer.definitions.sided;

import dev.creoii.dungeoneer.definitions.statuseffect.StatusEffect;
import dev.creoii.dungeoneer.definitions.statuseffect.StatusEffectInstance;

public interface LivingEntity extends Entity {
    /**
     * @return True if the effect was successfully added, False if not.
     */
    boolean addStatusEffect(StatusEffectInstance instance);

    default boolean addStatusEffect(StatusEffect effect) {
        return addStatusEffect(new StatusEffectInstance(effect, 0, 0, System.currentTimeMillis()));
    }

    default boolean addStatusEffect(StatusEffect effect, int duration) {
        return addStatusEffect(new StatusEffectInstance(effect, 0, duration, System.currentTimeMillis()));
    }

    default boolean addStatusEffect(StatusEffect effect, int amplifier, int duration) {
        return addStatusEffect(new StatusEffectInstance(effect, amplifier, duration, System.currentTimeMillis()));
    }

    /**
     * @return True if the effect was successfully removed, False if not.
     */
    boolean removeStatusEffect(StatusEffect statusEffect);

    boolean hasStatusEffect(StatusEffect statusEffect);

    void clearStatusEffects();
}
