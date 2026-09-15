package dev.creoii.dungeoneer.server.game;

import dev.creoii.dungeoneer.definitions.attack.bullet.Bullet;
import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.util.collision.Collidable;
import dev.creoii.dungeoneer.util.context.Context;
import dev.creoii.dungeoneer.util.event.HitEvents;

public class ServerBullet extends Bullet<ServerRaid> {
    private final ServerRaid raid;

    public ServerBullet(ServerRaid raid) {
        this.raid = raid;
    }

    @Override
    public ServerRaid getRaid() {
        return raid;
    }

    @Override
    public void setRaid(ServerRaid raid) {
    }

    @Override
    public void onCollisionEnter(Collidable other) {
        if (other instanceof Character<?> character) {
            if (!HitEvents.PRE.invoker().onPreHit(character, this, raid))
                return;
            if (character.damage(getDamage())) {
                HitEvents.POST.invoker().onPostHit(character, this, raid);
            }
        }
    }
}
