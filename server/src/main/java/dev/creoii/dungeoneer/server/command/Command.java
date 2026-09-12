package dev.creoii.dungeoneer.server.command;

import com.badlogic.gdx.utils.ObjectMap;
import dev.creoii.dungeoneer.server.DungeoneerServer;
import dev.creoii.dungeoneer.util.Identifiable;
import dev.creoii.dungeoneer.util.logging.Logger;

import java.util.Arrays;

public sealed interface Command extends Identifiable permits SimpleCommand, ParentCommand {
    Logger LOGGER = new Logger(Commands.class.getSimpleName());
    ObjectMap<String, Command> ALL = new ObjectMap<>();

    static void register(String id, Command command) {
        ALL.put(id, command);
    }

    static ParentCommand registerParent(String id) {
        ParentCommand command = new ParentCommand(id);
        ALL.put(id, command);
        return command;
    }

    static void register(String id, int minArgs, CommandExecutor executor) {
        register(id, new SimpleCommand(id, executor, minArgs));
    }

    static void register(String id, CommandExecutor executor) {
        register(id, 0, executor);
    }

    static Command.Result tryExecute(DungeoneerServer server, long accountId, long raidId, String commandType, String[] args) {
        if (raidId == -1) {
            return Command.Result.fail(commandType, args, "No raid target found: " + raidId);
        }

        if (ALL.containsKey(commandType)) {
            try {
                Command command = ALL.get(commandType);
                if (args.length <= command.minArgs() - 1) {
                    return Command.Result.fail(commandType, args, "Not enough arguments. Required " + command.minArgs() + ", received: " + args.length);
                }
                return command.execute(server, accountId, raidId, args);
            } catch (Exception e) {
                return Command.Result.fail(commandType, args, e.toString());
            }
        }
        return Command.Result.fail(commandType, args, "Command not found.");
    }

    int minArgs();

    Command.Result execute(DungeoneerServer server, long accountId, long raidId, String[] args);

    record Result(String message, boolean success) {
        public static Result success(String commandType, String[] args) {
            return new Result("Successfully executed '/" + commandType + "' with args '" + Arrays.toString(args) + "'", true);
        }

        public static Result fail(String commandType, String[] args, String error) {
            return new Result("Execution of '/" + commandType + "' with args '" + Arrays.toString(args) + "' failed: " + error, false);
        }
    }
}
