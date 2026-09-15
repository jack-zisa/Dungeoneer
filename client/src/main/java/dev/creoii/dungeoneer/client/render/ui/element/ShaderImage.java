package dev.creoii.dungeoneer.client.render.ui.element;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class ShaderImage extends Image {
    private final ShaderProgram shader;
    private final TextureRegion region;

    public ShaderImage(Texture texture, ShaderProgram shader) {
        super(texture);
        this.region = new TextureRegion(texture);
        this.shader = shader;
    }

    public void setTexture(Texture texture) {
        region.setTexture(texture);
        setDrawable(new TextureRegionDrawable(region));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        ShaderProgram previous = batch.getShader();
        batch.setShader(shader);
        super.draw(batch, parentAlpha);
        batch.setShader(previous);
    }
}
