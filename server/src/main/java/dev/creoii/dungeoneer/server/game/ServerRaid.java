package dev.creoii.dungeoneer.server.game;

import dev.creoii.dungeoneer.util.Tickable;

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
        character.getPos().mulAdd(character.getVelocity(), character.getSpeed() * dt);
    }
}
