package dev.creoii.dungeoneer.client.render.screen;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Tooltip;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;

public class FollowingTooltip<T extends Actor> extends Tooltip<T> {
    private final Vector2 position = new Vector2();

    public FollowingTooltip(T contents) {
        super(contents);
    }

    public FollowingTooltip(T contents, TooltipManager manager) {
        super(contents, manager);
    }

    @Override
    public boolean mouseMoved(InputEvent event, float x, float y) {
        Container<T> container = getContainer();
        position.set(x, y);
        event.getListenerActor().localToStageCoordinates(position);
        container.setPosition(position.x + 15f, position.y - container.getHeight() - 15f);
        return false;
    }
}
