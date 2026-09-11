package dev.creoii.dungeoneer;

import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.definitions.sided.LivingEntity;
import dev.creoii.dungeoneer.util.event.*;

public final class SharedInit {
    public static void initialize() {
        AttackEvents.PRE.register((entity, _, _) -> {
            return !(entity instanceof LivingEntity livingEntity) || !livingEntity.hasStatusEffect(DataManager.getStatusEffect("stunned"));
        });
        MoveEvents.PRE.register((entity, _) -> {
            return !(entity instanceof LivingEntity livingEntity) || !livingEntity.hasStatusEffect(DataManager.getStatusEffect("paralyzed"));
        });
        HitEvents.PRE.register((entity, _, _) -> {
            return !(entity instanceof LivingEntity livingEntity) || !livingEntity.hasStatusEffect(DataManager.getStatusEffect("invulnerable"));
        });
        DamageEvents.MODIFY.register((entity, damage) -> {
            if (!(entity instanceof LivingEntity livingEntity))
                return damage;

            if (livingEntity.hasStatusEffect(DataManager.getStatusEffect("vulnerable"))) {
                damage *= 2;
            } else if (livingEntity.hasStatusEffect(DataManager.getStatusEffect("armored"))) {
                damage /= 2;
            }

            if (entity instanceof Character<?> character) {
                float defense = character.getStats().defense().value();
                float minimumDamage = damage * .1f;
                damage = (int) (damage - defense);
                damage = (int) Math.max(damage, minimumDamage);
            }

            return damage;
        });
        HealEvents.MODIFY.register((entity, amount) -> {
            if (!(entity instanceof LivingEntity livingEntity))
                return amount;

            if (livingEntity.hasStatusEffect(DataManager.getStatusEffect("vitalized"))) {
                amount *= 2;
            } else if (livingEntity.hasStatusEffect(DataManager.getStatusEffect("sickened"))) {
                amount = 0;
            }
            return amount;
        });
    }
}
