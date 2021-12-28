package com.destrostudios.icetea.core.scene.gui;

import com.destrostudios.icetea.core.texture.Texture;
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
        material.setVertexShader(new Shader("com/destrostudios/icetea/core/shaders/default.vert", new String[] {
            "com/destrostudios/icetea/core/shaders/nodes/light.glsllib",
            "com/destrostudios/icetea/core/shaders/nodes/shadow.glsllib"
        }));
        material.setFragmentShader(new Shader("com/destrostudios/icetea/core/shaders/default.frag", new String[] {
            "com/destrostudios/icetea/core/shaders/nodes/light.glsllib",
            "com/destrostudios/icetea/core/shaders/nodes/shadow.glsllib"
        }));
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
