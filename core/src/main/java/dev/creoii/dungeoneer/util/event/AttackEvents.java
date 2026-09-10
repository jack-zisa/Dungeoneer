package dev.creoii.dungeoneer.util.event;

import dev.creoii.dungeoneer.definitions.attack.Attack;
import dev.creoii.dungeoneer.definitions.sided.Entity;
import dev.creoii.dungeoneer.definitions.sided.Raid;

public final class AttackEvents {
    public static final Event<Pre> PRE = Event.create(Pre.class, events -> (entity, attack, raid) -> {
        for (Pre event : events) {
            if (!event.onPreAttack(entity, attack, raid))
                return false;
        }
        return true;
    });

    public static final Event<Post> POST = Event.create(Post.class, events -> (entity, attack, raid) -> {
        for (Post event : events) {
            event.onPostAttack(entity, attack, raid);
        }
    });

    @FunctionalInterface
    public interface Pre {
        boolean onPreAttack(Entity entity, Attack attack, Raid<?, ?, ?, ?> raid);
    }

    @FunctionalInterface
    public interface Post {
        void onPostAttack(Entity entity, Attack attack, Raid<?, ?, ?, ?> raid);
    }
}
