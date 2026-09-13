package dev.creoii.dungeoneer.util.event;

import dev.creoii.dungeoneer.definitions.sided.Entity;
import dev.creoii.dungeoneer.definitions.sided.Raid;

public final class MoveEvents {
    public static final Event<Pre> PRE = Event.create(Pre.class, events -> (entity, raid) -> {
        for (Pre event : events) {
            if (!event.onPreMove(entity, raid))
                return false;
        }
        return true;
    });

    public static final Event<Post> POST = Event.create(Post.class, events -> (entity, raid) -> {
        for (Post event : events) {
            event.onPostMove(entity, raid);
        }
    });

    @FunctionalInterface
    public interface Pre {
        boolean onPreMove(Entity<?> entity, Raid<?, ?, ?, ?> raid);
    }

    @FunctionalInterface
    public interface Post {
        void onPostMove(Entity<?> entity, Raid<?, ?, ?, ?> raid);
    }
}
