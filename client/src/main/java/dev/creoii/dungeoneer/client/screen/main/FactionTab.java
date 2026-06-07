package dev.creoii.dungeoneer.client.screen.main;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.definitions.Account;
import dev.creoii.dungeoneer.definitions.Faction;
import dev.creoii.dungeoneer.network.c2s.faction.JoinFactionC2S;
import dev.creoii.dungeoneer.network.c2s.faction.LeaveFactionC2S;
import dev.creoii.dungeoneer.network.c2s.faction.SearchFactionC2S;

import java.util.List;

public class FactionTab extends Tab {
    private Label factionNameLabel;
    private Label factionDescriptionLabel;
    private TextField factionField;
    private Table membersTable;
    private ScrollPane membersScrollPane;

    private ScrollPane resultsScrollPane;
    private Table resultsTable;
    private TextButton clearResultsButton;
    private TextButton searchButton;

    private TextButton createButton;
    private TextButton leaveButton;

    protected FactionTab(Dungeoneer client, TextureRegion tabTexture) {
        super(client, tabTexture);
    }

    @Override
    protected void build() {
        Faction faction = getClient().getState().getFaction();
        factionNameLabel = new Label(faction == null ? "Join a Faction!" : faction.name(), getSkin());
        add(factionNameLabel).pad(10f).row();
        factionDescriptionLabel = new Label(faction == null ? "" : faction.description(), getSkin());
        add(factionDescriptionLabel).pad(10f).row();

        factionField = new TextField("", getSkin());
        add(factionField).pad(10f);

        membersTable = new Table();
        membersScrollPane = new ScrollPane(membersTable, getSkin());
        membersScrollPane.setFadeScrollBars(false);
        if (faction != null)
            refreshMembers(faction);
        add(membersScrollPane).grow().pad(10f).row();

        searchButton = new TextButton("Search", getSkin());
        searchButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getClient().get().sendUDP(new SearchFactionC2S(factionField.getText()));
            }
        });
        add(searchButton);

        resultsTable = new Table();
        resultsScrollPane = new ScrollPane(resultsTable, getSkin());
        resultsScrollPane.setFadeScrollBars(false);
        add(resultsScrollPane).grow().pad(10f).row();

        clearResultsButton = new TextButton("Clear Results", getSkin());
        clearResultsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                resultsTable.clearChildren();
            }
        });
        add(clearResultsButton).pad(10f).row();

        createButton = new TextButton("Create", getSkin());
        createButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (getClient().getState().getFaction() == null)
                    new CreateFactionDialog(getClient(), getSkin()).show(getStage());
            }
        });
        add(createButton).row();

        leaveButton = new TextButton("Leave", getSkin());
        leaveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (getClient().getState().getFaction() != null)
                    getClient().get().sendUDP(new LeaveFactionC2S(getClient().getState().getAccount()));
            }
        });
        add(leaveButton);
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

    @Override
    public void select() {
        Faction faction = getClient().getState().getFaction();
        if (faction != null) {
            factionNameLabel.setText(faction.name());
            factionDescriptionLabel.setText(faction.description());
            factionDescriptionLabel.setVisible(true);
            factionField.setVisible(false);

            refreshMembers(faction);
            membersTable.setVisible(true);
            membersScrollPane.setVisible(true);
            leaveButton.setVisible(true);
            createButton.setVisible(false);

            searchButton.setVisible(false);
            resultsTable.clearChildren();
            resultsTable.setVisible(false);
            clearResultsButton.setVisible(false);
            resultsScrollPane.setVisible(false);
        } else {
            factionNameLabel.setText("Join a Faction!");
            factionDescriptionLabel.setText("");
            factionDescriptionLabel.setVisible(false);
            factionField.setVisible(true);
            membersTable.setVisible(false);
            membersScrollPane.setVisible(false);
            leaveButton.setVisible(false);
            createButton.setVisible(true);

            searchButton.setVisible(true);
            resultsTable.setVisible(true);
            clearResultsButton.setVisible(true);
            resultsScrollPane.setVisible(true);
        }
    }
}
