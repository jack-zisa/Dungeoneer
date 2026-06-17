package dev.creoii.dungeoneer.definitions.attack;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.util.Identifiable;

import java.util.function.Function;

public sealed interface Attack extends Identifiable permits BulletAttack, CompositeAttack, LaserAttack, ReferenceAttack {
    AttackType type();

    Codec<Attack> CODEC = AttackType.CODEC.dispatch(Attack::type, type -> switch (type) {
        case BULLET -> BulletAttack.TYPE_CODEC;
        case LASER -> LaserAttack.TYPE_CODEC;
        case COMPOSITE -> CompositeAttack.TYPE_CODEC;
        case REFERENCE -> ReferenceAttack.TYPE_CODEC;
    });
    Codec<Attack> EITHER_CODEC = Codec.either(Codec.STRING, CODEC).xmap(either -> {
        return either.map(DataManager::getAttack, Function.identity());
    }, Either::right);

    static <T extends Attack> Products.P2<RecordCodecBuilder.Mu<T>, String, AttackType> addDefaultFields(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(Codec.STRING.fieldOf("id").forGetter(Attack::id), AttackType.CODEC.fieldOf("type").forGetter(Attack::type));
    }
}
