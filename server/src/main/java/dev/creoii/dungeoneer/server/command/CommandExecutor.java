package dev.creoii.dungeoneer.server.command;

import dev.creoii.dungeoneer.server.DungeoneerServer;

@FunctionalInterface
public interface CommandExecutor {
    Command.Result execute(DungeoneerServer server, long accountId, long raidId, String[] args);
}
