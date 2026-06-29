package dev.creoii.dungeoneer.client.render.screen.editor;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import dev.creoii.dungeoneer.client.ClientState;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.game.ClientDungeonMap;
import dev.creoii.dungeoneer.client.render.screen.AbstractScreen;
import dev.creoii.dungeoneer.client.render.screen.main.MainScreen;
import dev.creoii.dungeoneer.definitions.map.DungeonMapDefinition;

import java.time.LocalDateTime;

public class DungeonEditorScreen extends AbstractScreen {
    private Label selectedLayerLabel;
    private OrthographicCamera camera;
    private Sidebar sidebar;

    public DungeonEditorScreen(Dungeoneer client) {
        super(client);
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Sidebar getSidebar() {
        return sidebar;
    }

    public Label getSelectedLayerLabel() {
        return selectedLayerLabel;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.zoom = 1f;

        ClientDungeonMap dungeonMap = getClient().getState().getDungeonMap();
        if (dungeonMap.get() == null) {
            // TODO: Default map template
            getClient().getState().setDungeonMap(new DungeonMapDefinition(-1, getClient().getState().getAccount().id(), "circle", "necropolis", LocalDateTime.now()));
        }
        dungeonMap.build(getClient(), getClient().getState().getAccount().id(), dungeonMap.get().templateId(), dungeonMap.get().tilesetId());

        Table root = new Table();
        root.setFillParent(true);

        sidebar = new Sidebar(this);

        Table content = new Table();

        selectedLayerLabel = new Label(sidebar.getSelectedLayer(), SKIN);
        content.add(selectedLayerLabel).top().left().row();

        Table buttons = new Table();
        TextButton finishButton = new TextButton("Finish", SKIN);
        finishButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getClient().getState().getDungeonMap().save();
                getClient().setScreen(new MainScreen(getClient()));
                getClient().getState().setStatus(ClientState.Status.LOBBY);
            }
        });
        TextButton cancelButton = new TextButton("Cancel", SKIN);
        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getClient().setScreen(new MainScreen(getClient()));
                getClient().getState().setStatus(ClientState.Status.LOBBY);
            }
        });
        buttons.add(finishButton);
        buttons.add(cancelButton).padLeft(5f);

        content.add(buttons).expandY().bottom();

        root.add(sidebar).width(200f).fillY().left().top();
        root.add(content).grow();

        getStage().addActor(root);
        getClient().getInputMultiplexer().addProcessor(getStage());
        super.show();
    }

    @Override
    public void hide() {
        getClient().getInputMultiplexer().removeProcessor(getStage());
    }

    @Override
    public void render(float delta) {
        getClient().getState().getDungeonMap().getMapRenderer().setView(camera);
        getClient().getState().getDungeonMap().getMapRenderer().render();
        super.render(delta);
    }

    @Override
    public void dispose() {
        super.dispose();
        SKIN.dispose();
    }
}
