package dev.creoii.dungeoneer.definitions.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.definitions.attack.Attack;
import dev.creoii.dungeoneer.definitions.attack.bullet.BulletType;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.stat.StatContainer;

public record WeaponItem(String id, EquipmentType equipmentType, StatContainer statBonus, Attack attack, BulletType bullet) implements EquipmentItem {
    public static final MapCodec<WeaponItem> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
            Identifiable.idField(),
            EquipmentItem.equipmentTypeField(Type.WEAPON),
            EquipmentItem.statBonusField(),
            Attack.EITHER_CODEC.fieldOf("attack").forGetter(WeaponItem::attack),
            BulletType.EITHER_CODEC.fieldOf("bullet").forGetter(WeaponItem::bullet)
        ).apply(instance, WeaponItem::new);
    });

    @Override
    public Type type() {
        return Type.WEAPON;
    }

    @Override
    public Identifiable withId(String id) {
        return new WeaponItem(id, equipmentType, statBonus, attack, bullet);
    }
}
