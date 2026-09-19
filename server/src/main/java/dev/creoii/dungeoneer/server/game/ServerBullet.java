package dev.creoii.dungeoneer.server.game;

import dev.creoii.dungeoneer.definitions.attack.bullet.Bullet;
import dev.creoii.dungeoneer.definitions.sided.Character;
import dev.creoii.dungeoneer.definitions.sided.DungeonMap;
import dev.creoii.dungeoneer.network.s2c.raid.MoveEntitiesS2C;
import dev.creoii.dungeoneer.util.collision.Collidable;
import dev.creoii.dungeoneer.util.event.HitEvents;

public class ServerBullet extends Bullet<ServerRaid> implements ServerEntity {
    private ServerRaid raid;

    public ServerBullet(ServerRaid raid) {
        this.raid = raid;
    }

    @Override
    public ServerRaid getRaid() {
        return raid;
    }

    @Override
    public void setRaid(ServerRaid raid) {
        this.raid = raid;
    }

    @Override
    public void onCollisionEnter(Collidable other) {
        if (other instanceof Character<?> character) {
            if (!HitEvents.PRE.invoker().onPreHit(character, this, raid))
                return;
            if (character.damage(getDamage())) {
                HitEvents.POST.invoker().onPostHit(character, this, raid);
                setParent(null);
                setDead(true);
                raid.getRemoveEntityEntries().add(id());
            }
        }
    }

    @Override
    public boolean applyTransform(DungeonMap map, float originX, float originY, float dirX, float dirY) {
        if (super.applyTransform(map, originX, originY, dirX, dirY)) {
            raid.getMoveEntityEntries().add(new MoveEntitiesS2C.Entry(id(), getX(), getY()));
            return true;
        }
        return false;
    }
}
