package com.destrostudios.icetea.core.font;

import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.render.bucket.RenderBucketType;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.shader.Shader;

import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_FRONT_BIT;

public class BitmapText extends Geometry {

    public BitmapText(BitmapFont font) {
        this(font, "");
    }

    public BitmapText(BitmapFont font, String text) {
        setMesh(new BitmapTextMesh(font, text));

        Material material = new Material();
        material.setVertexShader(new Shader("shaders/default.vert", new String[] { "light", "shadow" }));
        material.setFragmentShader(new Shader("shaders/default.frag", new String[] { "light", "shadow" }));
        material.setCullMode(VK_CULL_MODE_FRONT_BIT);
        material.setDepthTest(false);
        material.setDepthWrite(false);
        material.setTransparent(true);
        setMaterial(material);
        updateMaterial(font);

        setRenderBucket(RenderBucketType.GUI);
    }

    public void setFont(BitmapFont font) {
        ((BitmapTextMesh) mesh).setFont(font);
        updateMaterial(font);
    }

    private void updateMaterial(BitmapFont font) {
        // TODO: Support multiple pages
        material.setTexture("diffuseMap", font.getTextures().get("0"));
    }

    public void setText(String text) {
        ((BitmapTextMesh) mesh).setText(text);
    }
}
