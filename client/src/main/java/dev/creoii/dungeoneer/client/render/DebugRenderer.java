package dev.creoii.dungeoneer.client.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.game.ClientCharacter;
import dev.creoii.dungeoneer.client.render.screen.AbstractScreen;

public class DebugRenderer {
    private static final int TEXT_PADDING = 10;
    private static final GlyphLayout DEBUG_LAYOUT = new GlyphLayout();
    private final Dungeoneer client;

    public DebugRenderer(Dungeoneer client) {
        this.client = client;
    }

    private String[] getDebugText() {
        ClientCharacter character = client.getState().getActiveCharacter();
        String serverPosText = String.format("  Server Pos: %.2f, %.2f", character.getX(), character.getY());
        String renderPosText = String.format("  Render Pos: %.2f, %.2f", character.getRenderX(), character.getRenderY());
        String tilePosText = String.format("  Tile: %d, %d", MathUtils.floor(character.getX() * .125f), MathUtils.floor(character.getY() * .125f));
        String statsText = character.getStats().toDebugString(character.getMaxStats());

        return new String[]{
            Gdx.graphics.getFramesPerSecond() + " FPS",
            serverPosText, renderPosText, tilePosText,
            statsText};
    }

    public void render(AbstractScreen screen) {
        String[] lines = getDebugText();

        Viewport viewport = screen.getStage().getViewport();

        float baseY = viewport.getWorldHeight() - TEXT_PADDING;
        float x;
        float y;

        for (int i = 0; i < lines.length; i++) {
            String text = lines[i];
            DEBUG_LAYOUT.setText(Assets.FONT, text);
            x = viewport.getWorldWidth() - DEBUG_LAYOUT.width - TEXT_PADDING;
            y = baseY - (i * 25);
            Assets.FONT.draw(screen.getStage().getBatch(), DEBUG_LAYOUT, x, y);
        }
    }
}
