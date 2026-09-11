package dev.creoii.dungeoneer.server.game;

import dev.creoii.dungeoneer.definitions.attack.bullet.Bullet;
import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.util.collision.Collidable;

public class ServerBullet extends Bullet {
    private final ServerRaid raid;

    public ServerBullet(ServerRaid raid) {
        this.raid = raid;
    }

    @Override
    public void onCollisionEnter(Collidable other) {
        if (other instanceof Character character) {
            int damage = 5;
            character.damage(damage);
        }
    }
}
