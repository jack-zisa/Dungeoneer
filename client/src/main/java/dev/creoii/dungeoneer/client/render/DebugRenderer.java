package dev.creoii.dungeoneer.client.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.client.game.ClientBullet;
import dev.creoii.dungeoneer.client.game.ClientBulletGroup;
import dev.creoii.dungeoneer.client.game.ClientCharacter;
import dev.creoii.dungeoneer.client.game.ClientRaid;
import dev.creoii.dungeoneer.client.render.ui.screen.AbstractScreen;
import dev.creoii.dungeoneer.client.render.ui.screen.game.GameScreen;
import dev.creoii.dungeoneer.definitions.map.MapLayerType;
import dev.creoii.dungeoneer.definitions.map.tile.Tile;

public class DebugRenderer {
    private static final int TEXT_PADDING = 10;
    private static final GlyphLayout DEBUG_LAYOUT = new GlyphLayout();
    private final Dungeoneer client;
    private ShapeRenderer shapeRenderer;

    public DebugRenderer(Dungeoneer client) {
        this.client = client;
    }

    public void create() {
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
    }

    private String[] getDebugText() {
        boolean inGame = !client.getState().getCurrentRaid().isNull();

        ClientCharacter character = client.getState().getActiveCharacter();
        String serverPosText = String.format("  Server Pos: %.2f, %.2f", character.getX(), character.getY());
        String renderPosText = String.format("  Render Pos: %.2f, %.2f", character.getRenderX(), character.getRenderY());
        Tile tile = inGame ? character.getTileOn(MapLayerType.GROUND) : null;
        String tilePosText = String.format("  Tile: %d, %d: %s", character.getTileX(), character.getTileY(), tile == null ? "" : tile.id());
        String statsText = character.getStats().toDebugString(character.get().characterClass().maxStats());

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

        if (screen instanceof GameScreen gameScreen) {
            Batch batch = screen.getStage().getBatch();

            batch.end();

            shapeRenderer.setProjectionMatrix(gameScreen.getCamera().combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            ClientRaid raid = client.getState().getCurrentRaid();
            if (raid != null) {
                raid.getEntityManager().getEntities().forEach(entity -> {
                    if (entity instanceof ClientBullet clientBullet) {
                        clientBullet.renderDebug(shapeRenderer, new float[0]);
                    } else if (entity instanceof ClientBulletGroup clientBulletGroup) {
                        clientBulletGroup.renderDebug(shapeRenderer, new float[0]);
                    }
                });
            }

            gameScreen.getVisibleCharacters().forEach(character -> character.renderDebug(shapeRenderer, gameScreen.getInputListener().getDirectionToMouse(character.getCenterX(), character.getCenterY())));

            shapeRenderer.end();

            batch.begin();
        }
    }
}
