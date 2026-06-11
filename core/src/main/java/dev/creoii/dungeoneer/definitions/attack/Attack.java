package dev.creoii.dungeoneer.definitions.attack;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.util.Identifiable;

public interface Attack extends Identifiable {
    AttackType type();

    Codec<Attack> CODEC = AttackType.CODEC.dispatch(Attack::type, type -> switch (type) {
        case BULLET -> BulletAttack.TYPE_CODEC;
    });
    Codec<Attack> ID_CODEC = Codec.STRING.xmap(DataManager::getAttack, Attack::id);

    static <T extends Attack> Products.P2<RecordCodecBuilder.Mu<T>, String, AttackType> addDefaultFields(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(Codec.STRING.fieldOf("id").forGetter(Attack::id), AttackType.CODEC.fieldOf("type").forGetter(Attack::type));
    }
}
