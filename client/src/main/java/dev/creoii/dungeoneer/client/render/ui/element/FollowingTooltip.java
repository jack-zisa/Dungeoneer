package dev.creoii.dungeoneer.client.render.ui.element;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
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

        float width = container.getWidth();
        float height = container.getHeight();
        float stageWidth = event.getStage().getWidth();
        float stageHeight = event.getStage().getHeight();

        float tooltipX = position.x + 15f;
        float tooltipY = position.y - height - 15f;

        tooltipX = MathUtils.clamp(tooltipX, 0f, stageWidth - width);
        tooltipY = MathUtils.clamp(tooltipY, 0f, stageHeight - height);

        container.setPosition(tooltipX, tooltipY);
        return false;
    }
}
