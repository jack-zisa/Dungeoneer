package dev.creoii.dungeoneer.client.screen.main;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.definitions.Faction;
import dev.creoii.dungeoneer.network.c2s.faction.CreateFactionC2S;
import dev.creoii.dungeoneer.network.c2s.faction.JoinFactionC2S;
import dev.creoii.dungeoneer.network.c2s.faction.LeaveFactionC2S;

public class FactionTab extends Tab {
    private Label factionNameLabel;
    private TextField factionField;
    private TextButton joinButton;
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

        factionField = new TextField("", getSkin());
        add(factionField).pad(10f).row();

        joinButton = new TextButton("Join", getSkin());
        joinButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (getClient().getState().getFaction() == null)
                    getClient().get().sendUDP(new JoinFactionC2S(getClient().getState().getAccount(), factionField.getText()));
            }
        });
        add(joinButton);

        createButton = new TextButton("Create", getSkin());
        createButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (getClient().getState().getFaction() == null)
                    getClient().get().sendUDP(new CreateFactionC2S(getClient().getState().getAccount(), factionField.getText()));
            }
        });
        add(createButton).row();

        leaveButton = new TextButton("leave", getSkin());
        leaveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (getClient().getState().getFaction() != null)
                    getClient().get().sendUDP(new LeaveFactionC2S(getClient().getState().getAccount()));
            }
        });
        add(leaveButton);
    }

    @Override
    public void select() {
        Faction faction = getClient().getState().getFaction();
        if (faction != null) {
            factionNameLabel.setText(faction.name());
            factionField.setVisible(false);
            joinButton.setVisible(false);
            leaveButton.setVisible(true);
            createButton.setVisible(false);
        } else {
            factionNameLabel.setText("Join a Faction!");
            factionField.setVisible(true);
            joinButton.setVisible(true);
            leaveButton.setVisible(false);
            createButton.setVisible(true);
        }
    }
}
