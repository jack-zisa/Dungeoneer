package dev.creoii.dungeoneer.server.game;

import dev.creoii.dungeoneer.util.Tickable;
import dev.creoii.dungeoneer.util.stat.StatUtils;

public class ServerRaid implements Tickable {
    private final ServerDungeon dungeon;
    private final ServerCharacter character;

    public ServerRaid(ServerDungeon dungeon, ServerCharacter character) {
        this.dungeon = dungeon;
        this.character = character;
    }

    public ServerDungeon getDungeon() {
        return dungeon;
    }

    public ServerCharacter getCharacter() {
        return character;
    }

    @Override
    public void tick(float dt) {
        float speed = StatUtils.getCalculatedSpeed(character.getStats().speed().value()) * dt;
        character.setPos(character.getX() + character.getVelocity()[0] * speed, character.getY() + character.getVelocity()[1] * speed);
    }
}
