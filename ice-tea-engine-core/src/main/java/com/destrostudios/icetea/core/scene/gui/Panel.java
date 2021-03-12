package com.destrostudios.icetea.core.scene.gui;

import com.destrostudios.icetea.core.Texture;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Quad;
import com.destrostudios.icetea.core.render.bucket.RenderBucketType;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.shader.Shader;

import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_FRONT_BIT;

public class Panel extends Geometry {

    public Panel() {
        setMesh(new Quad(1, 1));

        Material material = new Material();
        material.setVertexShader(new Shader("shaders/my_shader.vert", new String[] { "light", "shadow" }));
        material.setFragmentShader(new Shader("shaders/my_shader.frag", new String[] { "light", "shadow" }));
        material.setCullMode(VK_CULL_MODE_FRONT_BIT);
        material.setDepthTest(false);
        material.setDepthWrite(false);
        setMaterial(material);

        setRenderBucket(RenderBucketType.GUI);
    }

    public void setBackground(Texture texture) {
        getMaterial().setTexture("diffuseMap", texture);
    }
}
