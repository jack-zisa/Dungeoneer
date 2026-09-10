package dev.creoii.dungeoneer.definitions.sided;

import dev.creoii.dungeoneer.definitions.statuseffect.StatusEffect;
import dev.creoii.dungeoneer.definitions.statuseffect.StatusEffectInstance;

public interface LivingEntity extends Entity {
    /**
     * @return True if the effect was successfully added, False if not.
     */
    boolean addStatusEffect(StatusEffectInstance instance);
    
    /**
     * @return True if the effect was successfully removed, False if not.
     */
    boolean removeStatusEffect(StatusEffect statusEffect);

    boolean hasStatusEffect(StatusEffect statusEffect);
}
