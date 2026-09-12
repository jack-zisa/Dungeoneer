package dev.creoii.dungeoneer.server.command;

import dev.creoii.dungeoneer.server.DungeoneerServer;
import dev.creoii.dungeoneer.util.Identifiable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public record ParentCommand(String id, Map<String, SimpleCommand> children) implements Command {
    public ParentCommand(String id) {
        this(id, new HashMap<>());
    }

    @Override
    public int minArgs() {
        return 1;
    }

    @Override
    public Identifiable withId(String id) {
        return new ParentCommand(id, children);
    }

    public void registerChild(String childId, int minArgs, CommandExecutor executor) {
        children.put(childId, new SimpleCommand(childId, executor, minArgs));
    }

    public void registerChild(String childId, CommandExecutor executor) {
        registerChild(childId, 0, executor);
    }

    @Override
    public Command.Result execute(DungeoneerServer server, long accountId, long raidId, String[] args) {
        String childCommandType = args[0];
        args = Arrays.copyOfRange(args, 1, args.length);
        if (children.containsKey(childCommandType)) {
            try {
                SimpleCommand command = children.get(childCommandType);
                if (args.length <= command.minArgs() - 1) {
                    return Command.Result.fail(childCommandType, args, "Not enough arguments. Required " + command.minArgs() + ", received: " + args.length);
                }
                return command.execute(server, accountId, raidId, args);
            } catch (Exception e) {
                return Command.Result.fail(childCommandType, args, e.toString());
            }
        }
        return Command.Result.fail(childCommandType, args, "Command not found.");
    }
}
