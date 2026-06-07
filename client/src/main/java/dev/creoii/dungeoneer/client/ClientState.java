package dev.creoii.dungeoneer.client;

import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.definitions.Character;
import dev.creoii.dungeoneer.definitions.Faction;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ClientState {
    private Status status;
    private Account account;
    private List<Character> characters = new ArrayList<>();
    private Character selectedCharacter;
    private @Nullable Faction faction;

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
        this.characters = characters;
    }

    public void addCharacter(Character character) {
        characters.add(character);
    }

    public Character getSelectedCharacter() {
        return selectedCharacter;
    }

    public void setSelectedCharacter(Character selectedCharacter) {
        this.selectedCharacter = selectedCharacter;
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
