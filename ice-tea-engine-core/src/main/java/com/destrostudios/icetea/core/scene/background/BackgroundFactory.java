package com.destrostudios.icetea.core.scene.background;

import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Box;
import com.destrostudios.icetea.core.render.bucket.RenderBucketType;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.shader.FileShader;
import com.destrostudios.icetea.core.texture.Texture;

import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_FRONT_BIT;

public class BackgroundFactory {

    public static Geometry createCubeMapBackground(Texture backgroundMap) {
        Geometry geometry = new Geometry();
        geometry.setMesh(new Box(false, false));
        Material material = new Material();
        material.setDefaultShaders();
        material.setVertexShader(new FileShader("com/destrostudios/icetea/core/shaders/cubeMapBackground.vert"));
        material.setFragmentShader(new FileShader("com/destrostudios/icetea/core/shaders/cubeMapBackground.frag"));
        material.setTexture("backgroundMap", backgroundMap);
        material.setCullMode(VK_CULL_MODE_FRONT_BIT);
        material.setDepthWrite(false);
        geometry.setMaterial(material);
        geometry.setRenderBucket(RenderBucketType.BACKGROUND);
        return geometry;
    }
}
