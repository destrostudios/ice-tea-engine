package com.destrostudios.icetea.core.font;

import com.destrostudios.icetea.core.texture.Texture;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
public class BitmapFont {

    @Getter
    private int lineHeight;
    private Map<Character, BitmapFontCharacter> characters;
    @Getter
    private Map<String, Texture> textures;

    public BitmapFontCharacter getCharacter(char character) {
        return characters.get(character);
    }
}
