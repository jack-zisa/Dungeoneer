package dev.creoii.dungeoneer.client.render.ui.element;

import com.badlogic.gdx.scenes.scene2d.ui.Container;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.Dungeoneer;
import dev.creoii.dungeoneer.definitions.CharacterClass;

public class CharacterClassButton extends Container<ShaderImageButton> {
    private final CharacterClass characterClass;

    public CharacterClassButton(Dungeoneer client, CharacterClass characterClass) {
        ShaderImageButton button = new ShaderImageButton(client.getAssets().getTexture(Assets.Atlas.CHARACTER, characterClass.id()), Assets.BORDER_SHADER);
        button.getImageCell().size(40f, 40f);
        setActor(button);
        setSize(32f, 32f);
        this.characterClass = characterClass;
    }

    public CharacterClass getCharacterClass() {
        return characterClass;
    }
}
