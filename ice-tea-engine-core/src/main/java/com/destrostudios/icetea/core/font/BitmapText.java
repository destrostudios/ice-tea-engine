package com.destrostudios.icetea.core.font;

import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.scene.Geometry;
import lombok.Getter;

import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_FRONT_BIT;

public class BitmapText extends Geometry {

    public BitmapText(BitmapFont font) {
        this(font, "");
    }

    public BitmapText(BitmapFont font, String text) {
        this.font = font;
        this.text = text;
        setMesh(new BitmapTextMesh(font, text));

        Material material = new Material();
        material.setDefaultShaders();
        material.setCullMode(VK_CULL_MODE_FRONT_BIT);
        material.setDepthTest(false);
        material.setDepthWrite(false);
        material.setTransparent(true);
        setMaterial(material);
        updateMaterial(font);
    }
    @Getter
    private BitmapFont font;
    @Getter
    private String text;

    public void setFont(BitmapFont font) {
        if (!font.equals(this.font)) {
            this.font = font;
            updateMesh();
            updateMaterial(font);
        }
    }

    public void setText(String text) {
        if (!text.equals(this.text)) {
            this.text = text;
            updateMesh();
        }
    }

    private void updateMesh() {
        ((BitmapTextMesh) mesh).update(font, text);
    }

    private void updateMaterial(BitmapFont font) {
        // TODO: Support multiple pages
        material.setTexture("diffuseMap", font.getTextures().get("0"));
    }

    public int getTextWidth() {
        return font.getWidth(text);
    }

    public int getTextHeight() {
        return font.getHeight(text);
    }
}
