package dev.creoii.dungeoneer.server.command;

import com.badlogic.gdx.utils.ObjectMap;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.definitions.statuseffect.StatusEffect;
import dev.creoii.dungeoneer.network.s2c.character.StatUpdatesS2C;
import dev.creoii.dungeoneer.server.DungeoneerServer;
import dev.creoii.dungeoneer.server.game.ServerRaid;
import dev.creoii.dungeoneer.util.logging.Logger;
import dev.creoii.dungeoneer.util.stat.Stat;
import dev.creoii.dungeoneer.util.stat.StatContainer;

import javax.annotation.Nullable;

public final class Commands {
    public static final Logger LOGGER = new Logger(Commands.class.getSimpleName());
    static final ObjectMap<String, Command> ALL = new ObjectMap<>();

    @Nullable
    public static Command.Result tryExecute(DungeoneerServer server, long accountId, long raidId, String commandType, String[] args) {
        if (Commands.ALL.containsKey(commandType)) {
            try {
                Command command = Commands.ALL.get(commandType);
                if (args.length > command.minArgs() - 1) {
                    Command.Result result = command.execute(server, accountId, raidId, args);
                    if (result.success())
                        LOGGER.info(result.message());
                    else LOGGER.error(result.message());
                    return result;
                } else {
                    LOGGER.error(Command.Result.fail(commandType, args, "Not enough arguments. Required " + command.minArgs() + ", received: " + args.length).message());
                }
            } catch (Exception e) {
                LOGGER.error(Command.Result.fail(commandType, args, e.toString()).message());
                e.printStackTrace();
            }
        } else {
            LOGGER.error(Command.Result.fail(commandType, args, "Command not found.").message());
        }
        return null;
    }

    static {
        Command.register("addeffect", 1, (server, accountId, raidId, args) -> {
            String statusEffectId = args[0];
            StatusEffect effectType = DataManager.getStatusEffect(statusEffectId);

            if (effectType == null || raidId == -1) {
                return Command.Result.fail("addeffect", args, "Invalid effect type: " + effectType);
            }

            ServerRaid raid = server.getState().getRaids().get(raidId);
            if (raid == null || !raid.getCharacters().containsKey(accountId)) {
                return Command.Result.fail("addeffect", args, "Invalid target.");
            }

            int duration = 0;
            int amplifier = 0;

            if (args.length == 2) {
                duration = Integer.parseInt(args[1]);
            } else if (args.length == 3) {
                amplifier = Integer.parseInt(args[2]);
            }

            raid.getCharacterByAccountId(accountId).addStatusEffect(effectType, amplifier, duration);

            return Command.Result.success("addeffect", args);
        });

        Command.register("removeeffect", 1, (server, accountId, raidId, args) -> {
            String statusEffectId = args[0];
            StatusEffect effectType = DataManager.getStatusEffect(statusEffectId);

            if (effectType == null || raidId == -1) {
                return Command.Result.fail("removeeffect", args, "Invalid target.");
            }

            ServerRaid raid = server.getState().getRaids().get(raidId);
            if (raid == null || !raid.getCharacters().containsKey(accountId)) {
                return Command.Result.fail("removeeffect", args, "Invalid target.");
            }

            raid.getCharacterByAccountId(accountId).removeStatusEffect(effectType);

            return Command.Result.success("removeeffect", args);
        });

        Command.register("cleareffects", (server, accountId, raidId, args) -> {
            if (raidId == -1) {
                return Command.Result.fail("cleareffects", args, "Invalid target.");
            }

            ServerRaid raid = server.getState().getRaids().get(raidId);
            if (raid == null || !raid.getCharacters().containsKey(accountId)) {
                return Command.Result.fail("cleareffects", args, "Invalid target.");
            }

            raid.getCharacterByAccountId(accountId).clearStatusEffects();

            return Command.Result.success("cleareffects", args);
        });

        Command.register("setstat", 2, (server, accountId, raidId, args) -> {
            if (raidId == -1)
                return Command.Result.fail("setstat", args, "Invalid target.");

            ServerRaid raid = server.getState().getRaids().get(raidId);
            int connectionId = server.getSessionManager().getAccountConnections().getOrDefault(accountId, -1);
            if (raid == null || connectionId == -1 || raid.getCharacters().containsKey(accountId)) {
                return Command.Result.fail("setstat", args, "Invalid target.");
            }

            Stat.Type type = Stat.Type.valueOf(args[0].toUpperCase());
            int value = Integer.parseInt(args[1]);
            StatContainer stats = raid.getCharacterByAccountId(accountId).getStats();
            stats.setStat(type, value);
            server.get().sendToTCP(connectionId, new StatUpdatesS2C(accountId, stats));
            return Command.Result.success("setstat", args);
        });
    }
}
