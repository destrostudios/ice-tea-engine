package com.destrostudios.icetea.core.font;

import com.destrostudios.icetea.core.data.VertexData;
import com.destrostudios.icetea.core.mesh.Mesh;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class BitmapTextMesh extends Mesh {

    public BitmapTextMesh(BitmapFont font, String text) {
        update(font, text);
    }

    public void update(BitmapFont font, String text) {
        char[] characters = text.toCharArray();
        int renderedCharactersCount = 0;
        for (char character : characters) {
            if (character != '\n') {
                renderedCharactersCount++;
            }
        }
        vertices = new VertexData[renderedCharactersCount * 4];
        indices = new int[renderedCharactersCount * 6];
        int vertexIndex = 0;
        int indexIndex = 0;
        int x = 0;
        int y = 0;
        for (char character : characters) {
            if (character == '\n') {
                x = 0;
                y += font.getLineHeight();
                continue;
            }
            BitmapFontCharacter fontCharacter = font.getCharacter(character);

            int xLeft = (x + fontCharacter.getXOffset());
            int xRight = (xLeft + fontCharacter.getWidth());
            int yBottom = (y + fontCharacter.getYOffset());
            int yTop = (yBottom + fontCharacter.getHeight());

            float textureWidth = 256;
            float textureHeight = 256;
            float texCoordXLeft = (fontCharacter.getX() / textureWidth);
            float texCoordXRight = ((fontCharacter.getX() + fontCharacter.getWidth()) / textureWidth);
            float texCoordYTop = (fontCharacter.getY() / textureHeight);
            float texCoordYBottom = ((fontCharacter.getY() + fontCharacter.getHeight()) / textureHeight);

            VertexData vertex1 = new VertexData();
            vertex1.setVector3f("vertexPosition", new Vector3f(xLeft, yBottom, 0));
            vertex1.setVector2f("vertexTexCoord", new Vector2f(texCoordXLeft, texCoordYTop));
            vertex1.setVector3f("vertexNormal", new Vector3f(0, 0, 1));

            VertexData vertex2 = new VertexData();
            vertex2.setVector3f("vertexPosition", new Vector3f(xRight, yBottom, 0));
            vertex2.setVector2f("vertexTexCoord", new Vector2f(texCoordXRight, texCoordYTop));
            vertex2.setVector3f("vertexNormal", new Vector3f(0, 0, 1));

            VertexData vertex3 = new VertexData();
            vertex3.setVector3f("vertexPosition", new Vector3f(xRight, yTop, 0));
            vertex3.setVector2f("vertexTexCoord", new Vector2f(texCoordXRight, texCoordYBottom));
            vertex3.setVector3f("vertexNormal", new Vector3f(0, 0, 1));

            VertexData vertex4 = new VertexData();
            vertex4.setVector3f("vertexPosition", new Vector3f(xLeft, yTop, 0));
            vertex4.setVector2f("vertexTexCoord", new Vector2f(texCoordXLeft, texCoordYBottom));
            vertex4.setVector3f("vertexNormal", new Vector3f(0, 0, 1));

            indices[indexIndex++] = vertexIndex;
            indices[indexIndex++] = vertexIndex + 1;
            indices[indexIndex++] = vertexIndex + 2;
            indices[indexIndex++] = vertexIndex;
            indices[indexIndex++] = vertexIndex + 2;
            indices[indexIndex++] = vertexIndex + 3;

            vertices[vertexIndex++] = vertex1;
            vertices[vertexIndex++] = vertex2;
            vertices[vertexIndex++] = vertex3;
            vertices[vertexIndex++] = vertex4;

            x += fontCharacter.getXAdvance();
        }
        setBuffersOutdated();
        updateBounds();
    }
}
