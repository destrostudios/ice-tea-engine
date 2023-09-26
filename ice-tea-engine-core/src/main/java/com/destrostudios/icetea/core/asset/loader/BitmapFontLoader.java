package com.destrostudios.icetea.core.asset.loader;

import com.destrostudios.icetea.core.asset.AssetKey;
import com.destrostudios.icetea.core.asset.AssetLoader;
import com.destrostudios.icetea.core.asset.AssetManager;
import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.font.BitmapFontCharacter;
import com.destrostudios.icetea.core.texture.Texture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class BitmapFontLoader extends AssetLoader<BitmapFont, Void> {

    private String keyDirectory;

    @Override
    public void setContext(AssetManager assetManager, AssetKey assetKey, Void settings) {
        super.setContext(assetManager, assetKey, settings);
        // TODO: Share code between different loaders that need the directory for references
        int slashIndex = assetKey.getKey().lastIndexOf("/");
        if (slashIndex != -1) {
            keyDirectory = assetKey.getKey().substring(0, slashIndex + 1);
        } else {
            keyDirectory = "";
        }
    }

    @Override
    public BitmapFont load() throws IOException {
        int lineHeight = 0;
        HashMap<Character, BitmapFontCharacter> characters = new HashMap<>();
        HashMap<String, Texture> textures = new HashMap<>();
        try (InputStream inputStream = assetKey.openInputStream()) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split("[\\s=]+");
                switch (parts[0]) {
                    case "common": {
                        lineHeight = Integer.parseInt(parts[2]);
                        break;
                    }
                    case "page": {
                        String id = parts[2];
                        String file = parts[4];
                        if (file.startsWith("\"")) {
                            file = file.substring(1, file.length() - 1);
                        }
                        textures.put(id, assetManager.loadTexture(keyDirectory + file));
                        break;
                    }
                    case "char": {
                        Character id = (char) Integer.parseInt(parts[2]);
                        int x = Integer.parseInt(parts[4]);
                        int y = Integer.parseInt(parts[6]);
                        int width = Integer.parseInt(parts[8]);
                        int height = Integer.parseInt(parts[10]);
                        int xOffset = Integer.parseInt(parts[12]);
                        int yOffset = Integer.parseInt(parts[14]);
                        int xAdvance = Integer.parseInt(parts[16]);
                        characters.put(id, new BitmapFontCharacter(x, y, width, height, xOffset, yOffset, xAdvance));
                        break;
                    }
                }
            }
        }
        return new BitmapFont(lineHeight, characters, textures);
    }
}
