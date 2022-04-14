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

    public int getWidth(String text) {
        int maximumLineWidth = 0;
        int lineWidth = 0;
        for (char character : text.toCharArray()) {
            if (character == '\n') {
                if (lineWidth > maximumLineWidth) {
                    maximumLineWidth = lineWidth;
                }
                lineWidth = 0;
            } else {
                BitmapFontCharacter fontCharacter = getCharacter(character);
                lineWidth += fontCharacter.getXAdvance();
            }
        }
        if (lineWidth > maximumLineWidth) {
            maximumLineWidth = lineWidth;
        }
        return maximumLineWidth;
    }

    public int getHeight(String text) {
        return text.split("\n").length * lineHeight;
    }
}
