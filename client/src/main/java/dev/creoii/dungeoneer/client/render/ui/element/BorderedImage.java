package dev.creoii.dungeoneer.client.render.ui.element;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import dev.creoii.dungeoneer.client.Assets;

public class BorderedImage extends Image {
    private final Texture texture;

    public BorderedImage(Texture texture) {
        super(texture);
        this.texture = texture;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.setShader(Assets.BORDER_SHADER);

        Assets.BORDER_SHADER.setUniformf("u_pixelSize", (1f / texture.getWidth()) * .25f, (1f / texture.getHeight()) * .25f);
        Assets.BORDER_SHADER.setUniformf("u_borderColor", Color.BLACK);

        super.draw(batch, parentAlpha);

        batch.setShader(null);
    }
}
