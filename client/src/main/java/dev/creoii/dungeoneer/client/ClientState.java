package dev.creoii.dungeoneer.client;

import dev.creoii.dungeoneer.client.game.ClientCharacter;
import dev.creoii.dungeoneer.client.game.ClientRaid;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.definitions.Character;
import dev.creoii.dungeoneer.definitions.Faction;
import dev.creoii.dungeoneer.definitions.Raid;
import dev.creoii.dungeoneer.network.c2s.character.SelectActiveCharacterC2S;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ClientState {
    private final Dungeoneer client;
    private Status status;
    private Account account;
    private final List<Character> characters;
    private final ClientCharacter activeCharacter;
    private final ClientRaid currentRaid;
    private @Nullable Faction faction;

    public ClientState(Dungeoneer client) {
        this.client = client;
        characters = new ArrayList<>();
        activeCharacter = new ClientCharacter(null);
        currentRaid = new ClientRaid(null);
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

    public List<Character> getCharacters() {
        return characters;
    }

    public void setCharacters(List<Character> characters) {
        this.characters.clear();
        this.characters.addAll(characters);
    }

    public void addCharacter(Character character) {
        characters.add(character);
    }

    public ClientCharacter getActiveCharacter() {
        return activeCharacter;
    }

    public void setActiveCharacter(@Nullable Character activeCharacter) {
        this.activeCharacter.set(activeCharacter);
        client.get().sendUDP(new SelectActiveCharacterC2S(account.id(), activeCharacter == null ? -1L : activeCharacter.id()));
    }

    public ClientRaid getCurrentRaid() {
        return currentRaid;
    }

    public void setCurrentRaid(@Nullable Raid raid) {
        currentRaid.set(raid);
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
        AUTHENTICATING,
        LOBBY,
        RAID_SEARCHING,
        RAIDING
    }
}
