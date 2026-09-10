package dev.creoii.dungeoneer.client;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import dev.creoii.dungeoneer.DataManager;
import dev.creoii.dungeoneer.client.game.ClientCharacter;
import dev.creoii.dungeoneer.client.game.ClientDungeonMap;
import dev.creoii.dungeoneer.client.game.ClientRaid;
import dev.creoii.dungeoneer.client.render.screen.editor.ClientTiles;
import dev.creoii.dungeoneer.client.render.screen.game.RaidLoadingScreen;
import dev.creoii.dungeoneer.definitions.*;
import dev.creoii.dungeoneer.definitions.CharacterDefinition;
import dev.creoii.dungeoneer.definitions.map.DungeonMapDefinition;
import dev.creoii.dungeoneer.network.c2s.character.SelectActiveCharacterC2S;
import dev.creoii.dungeoneer.util.DungeonMapUtils;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ClientState {
    private final Dungeoneer client;
    private Status status;
    private Account account;
    private final ClientDungeonMap dungeonMap;
    private final List<CharacterDefinition> characters;
    private final ClientCharacter activeCharacter;
    private final ClientRaid currentRaid;
    private @Nullable Faction faction;

    public ClientState(Dungeoneer client) {
        this.client = client;
        dungeonMap = new ClientDungeonMap(client);
        characters = new ArrayList<>();
        activeCharacter = new ClientCharacter(client, null);
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

    public List<CharacterDefinition> getCharacters() {
        return characters;
    }

    public void setCharacters(List<CharacterDefinition> characters) {
        this.characters.clear();
        this.characters.addAll(characters);
    }

    public void addCharacter(CharacterDefinition character) {
        characters.add(character);
    }

    public ClientCharacter getActiveCharacter() {
        return activeCharacter;
    }

    public void setActiveCharacter(@Nullable CharacterDefinition activeCharacter) {
        this.activeCharacter.set(activeCharacter);
        client.get().sendTCP(new SelectActiveCharacterC2S(account.id(), activeCharacter == null ? -1L : activeCharacter.id()));
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

            Vector2 spawnPos = currentRaid.getDungeonMap().getTemplate().spawnPos();
            currentRaid.updateSpawnPositions(spawnPos.x, spawnPos.y);
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

    public void fillCharacters(int characterSlots) {
        for (int i = 0; i < characterSlots; ++i)
            characters.add(null);
    }

    public enum Status {
        STARTING,
        LOADING,
        AUTHENTICATING,
        LOBBY,
        RAID_SEARCHING,
        RAIDING,
        EDITING_DUNGEON,
        RAID_END
    }
}
