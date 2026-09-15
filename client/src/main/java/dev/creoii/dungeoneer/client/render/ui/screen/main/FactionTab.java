package dev.creoii.dungeoneer.client.render.ui.screen.main;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.render.ui.element.CreateFactionDialog;
import dev.creoii.dungeoneer.client.render.ui.screen.AbstractScreen;
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
    private Table activeTable;
    private Table factionTable;
    private Table factionSearchTable;

    private Label factionNameLabel;
    private Label factionDescriptionLabel;
    private TextField factionField;
    private Table membersTable;

    private Table resultsTable;

    private Table chatTable;
    private ScrollPane chatScrollPane;
    private TextField chatField;
    private long nextMessageId;

    protected FactionTab(Dungeoneer client, Texture tabTexture) {
        super(client, tabTexture);
    }

    @Override
    protected void build() {
        activeTable = new Table();
        add(activeTable).grow();

        factionTable = buildFactionTable();
        factionSearchTable = buildFactionSearchTable();
    }

    private Table buildFactionSearchTable() {
        Table factionSearchTable = new Table();
        factionSearchTable.defaults().pad(5f);

        factionSearchTable.add(new Label("Join a Faction!", AbstractScreen.SKIN)).row();

        factionField = new TextField("", AbstractScreen.SKIN);
        factionSearchTable.add(factionField).growX().row();

        TextButton searchButton = new TextButton("Search", AbstractScreen.SKIN);
        searchButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getClient().get().sendTCP(new SearchFactionC2S(factionField.getText()));
                factionField.setText("");
            }
        });
        factionSearchTable.add(searchButton).row();

        resultsTable = new Table();
        ScrollPane resultsScrollPane = new ScrollPane(resultsTable, AbstractScreen.SKIN);
        resultsScrollPane.setFadeScrollBars(false);
        factionSearchTable.add(resultsScrollPane).grow().row();

        TextButton clearResultsButton = new TextButton("Clear Results", AbstractScreen.SKIN);
        clearResultsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                resultsTable.clearChildren();
            }
        });
        factionSearchTable.add(clearResultsButton).row();

        TextButton createButton = new TextButton("Create", AbstractScreen.SKIN);
        createButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (getClient().getState().getFaction() == null)
                    new CreateFactionDialog(getClient()).show(getStage());
            }
        });
        factionSearchTable.add(createButton);

        return factionSearchTable;
    }

    private Table buildFactionTable() {
        Table factionTable = new Table();
        factionTable.defaults().pad(5f);

        factionNameLabel = new Label("", AbstractScreen.SKIN);
        factionDescriptionLabel = new Label("", AbstractScreen.SKIN);
        factionTable.add(factionNameLabel).row();
        factionTable.add(factionDescriptionLabel).row();

        chatTable = new Table();
        chatTable.top();
        chatScrollPane = new ScrollPane(chatTable, AbstractScreen.SKIN);
        chatScrollPane.setFadeScrollBars(false);
        factionTable.add(chatScrollPane).grow();

        membersTable = new Table();
        membersTable.add(new Label("Members", AbstractScreen.SKIN)).top().pad(4f).row();
        ScrollPane membersScrollPane = new ScrollPane(membersTable, AbstractScreen.SKIN);
        membersScrollPane.setFadeScrollBars(false);
        factionTable.add(membersScrollPane).width(100f).growY().row();

        chatField = new TextField("", AbstractScreen.SKIN);
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

        TextButton leaveButton = new TextButton("Leave", AbstractScreen.SKIN);
        leaveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (getClient().getState().getFaction() != null)
                    getClient().get().sendTCP(new LeaveFactionC2S(getClient().getState().getAccount()));
            }
        });
        factionTable.add(leaveButton);

        return factionTable;
    }

    public void refreshSearchResults(List<Faction> factions) {
        resultsTable.clearChildren();

        if (factions.isEmpty()) {
            resultsTable.add(new Label("No factions found.", AbstractScreen.SKIN));
            return;
        }

        for (Faction faction : factions) {
            Table row = new Table();

            row.add(new Label(faction.name(), AbstractScreen.SKIN)).width(150).left();
            row.add(new Label(faction.description(), AbstractScreen.SKIN)).width(300).left();
            row.add(new Label(faction.accounts().size() + " members", AbstractScreen.SKIN)).width(100);

            TextButton joinButton = new TextButton("Join", AbstractScreen.SKIN);
            joinButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    getClient().get().sendTCP(new JoinFactionC2S(getClient().getState().getAccount(), faction.id()));
                }
            });
            row.add(joinButton).padLeft(10f);

            resultsTable.add(row).growX().pad(5f).row();
        }
    }

    public void refreshMembers(Faction faction) {
        membersTable.clearChildren();
        membersTable.add(new Label("Members", AbstractScreen.SKIN)).top().pad(4f).row();
        for (Account account : faction.accounts()) {
            Table row = new Table();

            row.add(new Label(account.username(), AbstractScreen.SKIN));

            membersTable.add(row).growX().pad(5f).row();
        }
    }

    public void refreshChat() {
        chatTable.clearChildren();

        Faction faction = getClient().getState().getFaction();
        if (faction == null)
            return;

        for (Message message : faction.recentMessages().values()) {
            if (message.accountId() != -1L) {
                Optional<Account> account = getClient().getState().getFaction().accounts().stream().filter(account1 -> account1.id() == message.accountId()).findFirst();
                account.ifPresent(value -> {
                    Label messageLabel = new Label(String.format("%s: %s", value.username(), message.text()), AbstractScreen.SKIN);
                    chatTable.add(messageLabel).left().growX().row();
                });
            } else {
                Label messageLabel = new Label(message.text(), AbstractScreen.SKIN);
                messageLabel.setColor(Color.YELLOW);
                chatTable.add(messageLabel).left().growX().row();
            }
        }

        chatScrollPane.layout();
        chatScrollPane.setScrollPercentY(1f);
    }

    private void showFactionSearch() {
        activeTable.clearChildren();
        activeTable.add(factionSearchTable).grow();
    }

    private void showFaction(Faction faction) {
        activeTable.clearChildren();
        activeTable.add(factionTable).grow();

        factionNameLabel.setText(faction.name());
        factionDescriptionLabel.setText(faction.description());

        refreshMembers(faction);
        refreshChat();
    }

    @Override
    public void select() {
        activeTable.clearChildren();

        Faction faction = getClient().getState().getFaction();
        if (faction != null) {
            showFaction(faction);
        } else {
            showFactionSearch();
        }
    }
}
