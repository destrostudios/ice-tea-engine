package com.destrostudios.icetea.core.render.fullscreen;

import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.texture.Texture;
import com.destrostudios.icetea.core.material.descriptor.SimpleTextureDescriptor;
import com.destrostudios.icetea.core.material.descriptor.SimpleTextureDescriptorLayout;

import java.util.function.Supplier;

public class FullScreenTextureRenderJob extends FullScreenQuadRenderJob {

    public FullScreenTextureRenderJob(Texture texture) {
        this(() -> texture);
    }

    public FullScreenTextureRenderJob(Supplier<Texture> textureSupplier) {
        this.textureSupplier = textureSupplier;
    }
    private Supplier<Texture> textureSupplier;

    @Override
    protected void fillMaterialDescriptorLayoutAndSet() {
        materialDescriptorSetLayout.addDescriptorLayout(new SimpleTextureDescriptorLayout());
        materialDescriptorSet.addDescriptor(new SimpleTextureDescriptor("colorMap", textureSupplier.get()));
    }

    @Override
    public Shader getFragmentShader() {
        return new Shader("com/destrostudios/icetea/core/shaders/fullScreenTexture.frag");
    }
}
