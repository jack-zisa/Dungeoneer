package dev.creoii.dungeoneer.client;

import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.definitions.Character;
import dev.creoii.dungeoneer.definitions.Faction;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ClientState {
    private Account account;
    private List<Character> characters = new ArrayList<>();
    private Character selectedCharacter;
    private @Nullable Faction faction;

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
}
