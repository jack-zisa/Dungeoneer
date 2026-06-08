package dev.creoii.dungeoneer.client.screen.main;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.definitions.Faction;
import dev.creoii.dungeoneer.definitions.Message;
import dev.creoii.dungeoneer.network.c2s.faction.ChatMessageC2S;
import dev.creoii.dungeoneer.network.c2s.faction.JoinFactionC2S;
import dev.creoii.dungeoneer.network.c2s.faction.LeaveFactionC2S;
import dev.creoii.dungeoneer.network.c2s.faction.SearchFactionC2S;

import java.util.List;
import java.util.Optional;

public class FactionTab extends Tab {
    private Table factionSearchTable;
    private Table factionTable;

    private Label factionNameLabel;
    private Label factionDescriptionLabel;
    private TextField factionField;
    private Table membersTable;

    private Table resultsTable;

    private Table chatTable;
    private ScrollPane chatScrollPane;
    private TextField chatField;
    private long nextMessageId;

    protected FactionTab(Dungeoneer client, TextureRegion tabTexture) {
        super(client, tabTexture);
    }

    @Override
    protected void build() {
        Faction faction = getClient().getState().getFaction();

        factionSearchTable = new Table();
        factionSearchTable.defaults().pad(5f);

        factionSearchTable.add(new Label("Join a Faction!", getSkin())).row();

        factionField = new TextField("", getSkin());
        factionSearchTable.add(factionField).growX().row();

        TextButton searchButton = new TextButton("Search", getSkin());
        searchButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getClient().get().sendUDP(new SearchFactionC2S(factionField.getText()));
            }
        });
        factionSearchTable.add(searchButton).row();

        resultsTable = new Table();
        ScrollPane resultsScrollPane = new ScrollPane(resultsTable, getSkin());
        resultsScrollPane.setFadeScrollBars(false);
        factionSearchTable.add(resultsScrollPane).grow().row();

        TextButton clearResultsButton = new TextButton("Clear Results", getSkin());
        clearResultsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                resultsTable.clearChildren();
            }
        });
        factionSearchTable.add(clearResultsButton).row();

        TextButton createButton = new TextButton("Create", getSkin());
        createButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (getClient().getState().getFaction() == null)
                    new CreateFactionDialog(getClient(), getSkin()).show(getStage());
            }
        });
        factionSearchTable.add(createButton);

        factionTable = new Table();
        factionTable.defaults().pad(5f);

        factionNameLabel = new Label("", getSkin());
        factionDescriptionLabel = new Label("", getSkin());
        factionTable.add(factionNameLabel).row();
        factionTable.add(factionDescriptionLabel).row();

        membersTable = new Table();
        ScrollPane membersScrollPane = new ScrollPane(membersTable, getSkin());
        membersScrollPane.setFadeScrollBars(false);
        factionTable.add(membersScrollPane).height(150f).growX().row();

        chatTable = new Table();
        chatTable.top();
        chatScrollPane = new ScrollPane(chatTable, getSkin());
        chatScrollPane.setFadeScrollBars(false);
        factionTable.add(chatScrollPane).grow().row();

        chatField = new TextField("", getSkin());
        chatField.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if ((keycode == Input.Keys.ENTER || keycode == Input.Keys.NUMPAD_ENTER) && !chatField.getText().isBlank()) {
                    Message message = new Message(nextMessageId, getClient().getState().getFaction().id(), getClient().getState().getAccount().id(), chatField.getText(), false);
                    getClient().getState().getFaction().recentMessages().put(nextMessageId, message);
                    refreshChat();
                    getClient().get().sendTCP(new ChatMessageC2S(nextMessageId++, message));
                    chatField.setText("");
                    return true;
                }
                return false;
            }
        });
        factionTable.add(chatField).growX().row();

        TextButton leaveButton = new TextButton("Leave", getSkin());
        leaveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (getClient().getState().getFaction() != null)
                    getClient().get().sendUDP(new LeaveFactionC2S(getClient().getState().getAccount()));
            }
        });
        factionTable.add(leaveButton);

        add(factionSearchTable).grow();
        add(factionTable).grow();
    }

    public void refreshSearchResults(List<Faction> factions) {
        resultsTable.clearChildren();

        if (factions.isEmpty()) {
            resultsTable.add(new Label("No factions found.", getSkin()));
            return;
        }

        for (Faction faction : factions) {
            Table row = new Table();

            row.add(new Label(faction.name(), getSkin())).width(150).left();
            row.add(new Label(faction.description(), getSkin())).width(300).left();
            row.add(new Label(faction.accounts().size() + " members", getSkin())).width(100);

            TextButton joinButton = new TextButton("Join", getSkin());
            joinButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    getClient().get().sendUDP(new JoinFactionC2S(getClient().getState().getAccount(), faction.id()));
                }
            });
            row.add(joinButton).padLeft(10f);

            resultsTable.add(row).growX().pad(5f).row();
        }
    }

    public void refreshMembers(Faction faction) {
        membersTable.clearChildren();
        for (Account account : faction.accounts()) {
            Table row = new Table();

            row.add(new Label(account.username(), getSkin()));

            membersTable.add(row).growX().pad(5f).row();
        }
    }

    public void refreshChat() {
        chatTable.clearChildren();

        Faction faction = getClient().getState().getFaction();
        if (faction == null)
            return;

        for (Message message : faction.recentMessages().values()) {
            Optional<Account> account = getClient().getState().getFaction().accounts().stream().filter(account1 -> account1.id() == message.accountId()).findFirst();
            String username;
            if (account.isPresent()) {
                 username = account.get().username();
            } else username = "Unknown";
            chatTable.add(new Label(username + ": " + message.text(), getSkin())).left().growX().row();
        }

        chatScrollPane.layout();
        chatScrollPane.setScrollPercentY(1f);
    }

    @Override
    public void select() {
        Faction faction = getClient().getState().getFaction();
        boolean inFaction = faction != null;

        factionSearchTable.setVisible(!inFaction);
        factionTable.setVisible(inFaction);

        if (inFaction) {
            factionNameLabel.setText(faction.name());
            factionDescriptionLabel.setText(faction.description());
            refreshMembers(faction);
            refreshChat();
        }
    }
}
