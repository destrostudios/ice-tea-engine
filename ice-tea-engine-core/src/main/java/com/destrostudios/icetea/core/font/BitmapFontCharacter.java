package com.destrostudios.icetea.core.font;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BitmapFontCharacter {
    private int x;
    private int y;
    private int width;
    private int height;
    private int xOffset;
    private int yOffset;
    private int xAdvance;
}
