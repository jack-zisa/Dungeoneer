package dev.creoii.dungeoneer.client;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.client.game.ClientCharacter;
import dev.creoii.dungeoneer.client.game.ClientDungeonMap;
import dev.creoii.dungeoneer.client.game.ClientRaid;
import dev.creoii.dungeoneer.client.render.ui.editor.ClientTiles;
import dev.creoii.dungeoneer.client.render.ui.screen.game.RaidLoadingScreen;
import dev.creoii.dungeoneer.definitions.*;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.map.DungeonMapDefinition;
import dev.creoii.dungeoneer.network.c2s.character.SelectActiveCharacterC2S;
import dev.creoii.dungeoneer.util.DungeonMapUtils;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientState {
    private final Dungeoneer client;
    private Status status;
    private Account account;
    private final ClientDungeonMap dungeonMap;
    private final Map<Integer, ClientCharacter> characters;
    private final ClientRaid currentRaid;
    private int activeCharacter;
    private @Nullable Faction faction;

    public ClientState(Dungeoneer client) {
        this.client = client;
        dungeonMap = new ClientDungeonMap(client);
        characters = new HashMap<>();
        activeCharacter = 0;
        currentRaid = new ClientRaid(client, null);
        setStatus(ClientState.Status.STARTING);
    }

    public void setStatus(Status status) {
        this.status = status;
        Dungeoneer.LOGGER.info("Set status to: %s", status.name());
    }

    public Status getStatus() {
        return status;
    }

    public void setAccount(Account account) {
        this.account = account;
        for (int i = 0; i < account.characterSlots(); ++i) {
            characters.put(i, new ClientCharacter(client, null));
        }
    }

    public Account getAccount() {
        return account;
    }

    public ClientDungeonMap getEditorDungeonMap() {
        return dungeonMap;
    }

    public void setDungeonMap(DungeonMapDefinition definition) {
        dungeonMap.set(definition);
        dungeonMap.setMapRenderer(new OrthogonalTiledMapRenderer(DungeonMapUtils.deserializeMap2(definition.accountId(), DataManager.getMapTemplate(dungeonMap.get().templateId()), dungeonMap.get().tilesetId(), ClientTiles.TILESET, ClientTiles.SETTER)));
    }

    public Map<Integer, ClientCharacter> getCharacters() {
        return characters;
    }

    @Nullable
    public ClientCharacter getCharacter(int index) {
        return characters.get(index);
    }

    @Nullable
    public ClientCharacter getCharacterById(int characterId) {
        for (int i = 0; i < characters.size(); ++i) {
            ClientCharacter character = characters.get(i);
            if (character.isNull()) continue;
            if (character.get().id() == characterId) return character;
        }
        return null;
    }

    public void setCharacters(List<CharacterDefinition> characters) {
        this.characters.values().forEach(clientCharacter -> clientCharacter.set(null));
        for (int i = 0; i < characters.size(); ++i) {
            if (this.characters.containsKey(i)) {
                this.characters.get(i).set(characters.get(i));
            }
        }
    }

    @Nullable
    public ClientCharacter getActiveCharacter() {
        return characters.getOrDefault(activeCharacter, null);
    }

    public void setActiveCharacter(int activeCharacter) {
        this.activeCharacter = activeCharacter;
        ClientCharacter active = getActiveCharacter();
        client.get().sendTCP(new SelectActiveCharacterC2S(account.id(), active == null || active.isNull() ? -1L : active.get().id()));
    }

    public boolean hasActiveCharacter() {
        return characters.containsKey(activeCharacter) && !getActiveCharacter().isNull();
    }

    public ClientRaid getCurrentRaid() {
        return currentRaid;
    }

    public void setCurrentRaid(@Nullable RaidDefinition raid, String templateId, String tilesetId) {
        currentRaid.set(raid);

        if (raid == null) currentRaid.getDungeonMap().clear();
        else {
            syncRaid(raid);
            currentRaid.getDungeonMap().build(client, currentRaid.get().target().id(), templateId, tilesetId);
        }
    }

    public void syncRaid(RaidDefinition raid) {
        if (client.getScreen() instanceof RaidLoadingScreen raidLoadingScreen) {
            raidLoadingScreen.sync(raid);
        }
    }

    public @Nullable Faction getFaction() {
        return faction;
    }

    public void setFaction(@Nullable Faction faction) {
        this.faction = faction;
    }

    public enum Status {
        STARTING,
        LOADING,
        AUTHENTICATING,
        LOBBY(true),
        RAID_SEARCHING(true),
        RAIDING(true),
        EDITING_DUNGEON(true),
        RAID_END(true);

        private final boolean loaded;

        Status(boolean loaded) {
            this.loaded = loaded;
        }

        Status() {
            this(false);
        }

        public boolean isLoaded() {
            return loaded;
        }
    }
}
