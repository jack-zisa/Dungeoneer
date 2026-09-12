package dev.creoii.dungeoneer.server.game;

import dev.creoii.dungeoneer.definitions.attack.bullet.Bullet;
import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.util.collision.Collidable;
import dev.creoii.dungeoneer.util.event.HitEvents;

public class ServerBullet extends Bullet {
    private final ServerRaid raid;

    public ServerBullet(ServerRaid raid) {
        this.raid = raid;
    }

    @Override
    public void onCollisionEnter(Collidable other) {
        if (other instanceof Character<?> character) {
            if (!HitEvents.PRE.invoker().onPreHit(character, this, raid))
                return;
            int damage = 5;
            if (character.damage(damage)) {
                HitEvents.POST.invoker().onPostHit(character, this, raid);
            }
        }
    }
}
