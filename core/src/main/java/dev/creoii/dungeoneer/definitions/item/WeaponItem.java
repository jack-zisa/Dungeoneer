package dev.creoii.dungeoneer.definitions.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.attack.Attack;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.stat.StatContainer;

public record WeaponItem(String id, StatContainer statBonus, Attack attack) implements EquipmentItem {
    public static final MapCodec<WeaponItem> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
            Identifiable.idField(),
            EquipmentItem.statBonusField(),
            Attack.CODEC.fieldOf("attack").forGetter(WeaponItem::attack)
        ).apply(instance, WeaponItem::new);
    });

    @Override
    public Type type() {
        return Type.WEAPON;
    }

    @Override
    public Identifiable withId(String id) {
        return new WeaponItem(id, statBonus, attack);
    }
}
