package dev.creoii.dungeoneer.server.command;

import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.definitions.inventory.EquipmentInventory;
import dev.creoii.dungeoneer.definitions.inventory.Slot;
import dev.creoii.dungeoneer.definitions.item.Item;
import dev.creoii.dungeoneer.definitions.statuseffect.StatusEffect;
import dev.creoii.dungeoneer.network.s2c.character.StatUpdatesS2C;
import dev.creoii.dungeoneer.network.s2c.character.SyncEquipmentS2C;
import dev.creoii.dungeoneer.server.game.ServerRaid;
import dev.creoii.dungeoneer.util.stat.Stat;
import dev.creoii.dungeoneer.util.stat.StatContainer;

import java.util.List;

public final class Commands {
    public static void register() {
        ParentCommand effectCommand = Command.registerParent("effect");
        effectCommand.registerChild("add", 1, (server, accountId, raidId, args) -> {
            String statusEffectId = args[0];
            StatusEffect effectType = DataManager.getStatusEffect(statusEffectId);

            if (effectType == null) {
                return Command.Result.fail("effect add", args, "Effect " + statusEffectId + " does not exist.");
            }

            ServerRaid raid = server.getState().getRaids().get(raidId);
            if (raid == null || !raid.getCharacters().containsKey(accountId)) {
                return Command.Result.fail("effect add", args, "Invalid target.");
            }

            int duration = 0;
            int amplifier = 0;

            if (args.length == 2) {
                duration = Integer.parseInt(args[1]);
            } else if (args.length == 3) {
                amplifier = Integer.parseInt(args[2]);
            }

            raid.getCharacterByAccountId(accountId).addStatusEffect(effectType, amplifier, duration);

            return Command.Result.success("effect add", args);
        });

        effectCommand.registerChild("remove", 1, (server, accountId, raidId, args) -> {
            String statusEffectId = args[0];
            StatusEffect effectType = DataManager.getStatusEffect(statusEffectId);

            if (effectType == null) {
                return Command.Result.fail("effect remove", args, "Effect " + statusEffectId + " does not exist.");
            }

            ServerRaid raid = server.getState().getRaids().get(raidId);
            if (raid == null || !raid.getCharacters().containsKey(accountId)) {
                return Command.Result.fail("effect remove", args, "Invalid target.");
            }

            raid.getCharacterByAccountId(accountId).removeStatusEffect(effectType);

            return Command.Result.success("effect remove", args);
        });

        effectCommand.registerChild("clear", (server, accountId, raidId, args) -> {
            ServerRaid raid = server.getState().getRaids().get(raidId);
            if (raid == null || !raid.getCharacters().containsKey(accountId)) {
                return Command.Result.fail("effect clear", args, "Invalid target.");
            }

            raid.getCharacterByAccountId(accountId).clearStatusEffects();
            return Command.Result.success("effect clear", args);
        });

        ParentCommand statCommand = Command.registerParent("stat");
        statCommand.registerChild("set", 2, (server, accountId, raidId, args) -> {
            ServerRaid raid = server.getState().getRaids().get(raidId);
            int connectionId = server.getSessionManager().getAccountConnections().getOrDefault(accountId, -1);
            if (raid == null || connectionId == -1 || raid.getCharacters().containsKey(accountId)) {
                return Command.Result.fail("set stat", args, "Invalid target.");
            }

            Stat.Type type = Stat.Type.valueOf(args[0].toUpperCase());
            int value = Integer.parseInt(args[1]);
            StatContainer stats = raid.getCharacterByAccountId(accountId).getStats();
            stats.setStat(type, value);
            server.get().sendToTCP(connectionId, new StatUpdatesS2C(accountId, stats));
            return Command.Result.success("set stat", args);
        });

        ParentCommand inventoryCommand = Command.registerParent("inventory");
        inventoryCommand.registerChild("add", 1, (server, accountId, raidId, args) -> {
            ServerRaid raid = server.getState().getRaids().get(raidId);
            if (raid == null || !raid.getCharacters().containsKey(accountId)) {
                return Command.Result.fail("inventory add", args, "Invalid target.");
            }

            Item item = DataManager.getItem(args[0]);
            if (item == null) {
                return Command.Result.fail("inventory add", args, "Item " + args[0] + " does not exist.");
            }

            EquipmentInventory equipment = raid.getCharacterByAccountId(accountId).getEquipment();
            Slot slot = equipment.getNextAvailableSlot();
            if (equipment.addItem(item)) {
                raid.getCharacters().values().forEach(serverCharacter -> {
                    server.get().sendToTCP(serverCharacter.getConnectionId(), new SyncEquipmentS2C(accountId, List.of(slot)));
                });
                return Command.Result.success("inventory add", args);
            }
            return Command.Result.fail("inventory add", args, "Failed to add item.");
        });

        inventoryCommand.registerChild("clear", (server, accountId, raidId, args) -> {
            ServerRaid raid = server.getState().getRaids().get(raidId);
            if (raid == null || !raid.getCharacters().containsKey(accountId)) {
                return Command.Result.fail("inventory clear", args, "Invalid target.");
            }

            EquipmentInventory equipment = raid.getCharacterByAccountId(accountId).getEquipment();
            equipment.clear();
            return Command.Result.success("inventory clear", args);
        });
    }
}
