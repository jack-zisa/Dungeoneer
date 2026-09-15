package dev.creoii.dungeoneer.client.render.ui.element;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class ShaderImageButton extends ImageButton {
    private final ShaderProgram shader;

    public ShaderImageButton(Texture texture, ShaderProgram shader) {
        this(new TextureRegion(texture), shader);
    }

    public ShaderImageButton(TextureRegion region, ShaderProgram shader) {
        super(new TextureRegionDrawable(region));
        this.shader = shader;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        ShaderProgram previous = batch.getShader();
        batch.setShader(shader);
        super.draw(batch, parentAlpha);
        batch.setShader(previous);
    }

    @Override
    public void setColor(Color color) {
        super.setColor(color);
        getImage().setColor(color);
    }
}
