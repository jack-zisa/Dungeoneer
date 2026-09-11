package dev.creoii.dungeoneer.util.event;

import dev.creoii.dungeoneer.definitions.sided.BulletNode;
import dev.creoii.dungeoneer.definitions.sided.Entity;
import dev.creoii.dungeoneer.definitions.sided.Raid;

public final class HitEvents {
    public static final Event<Pre> PRE = Event.create(Pre.class, events -> (entity, bullet, raid) -> {
        for (Pre event : events) {
            if (!event.onPreHit(entity, bullet, raid))
                return false;
        }
        return true;
    });

    public static final Event<Post> POST = Event.create(Post.class, events -> (entity, bullet, raid) -> {
        for (Post event : events) {
            event.onPostHit(entity, bullet, raid);
        }
    });

    @FunctionalInterface
    public interface Pre {
        boolean onPreHit(Entity entity, BulletNode<?> bullet, Raid<?, ?, ?, ?> raid);
    }

    @FunctionalInterface
    public interface Post {
        void onPostHit(Entity entity, BulletNode<?> bullet, Raid<?, ?, ?, ?> raid);
    }
}
