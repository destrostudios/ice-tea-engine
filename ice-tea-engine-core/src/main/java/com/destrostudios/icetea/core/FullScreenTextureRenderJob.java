package com.destrostudios.icetea.core;

import com.destrostudios.icetea.core.materials.descriptors.SimpleTextureDescriptor;
import com.destrostudios.icetea.core.materials.descriptors.SimpleTextureDescriptorLayout;

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
        SimpleTextureDescriptorLayout colorTextureDescriptorLayout = new SimpleTextureDescriptorLayout();
        SimpleTextureDescriptor colorTextureDescriptor = new SimpleTextureDescriptor("colorMap", colorTextureDescriptorLayout, textureSupplier.get());
        materialDescriptorSetLayout.addDescriptorLayout(colorTextureDescriptorLayout);
        materialDescriptorSet.addDescriptor(colorTextureDescriptor);
    }

    @Override
    public Shader getFragmentShader() {
        return new Shader("shaders/fullScreenTexture.frag");
    }
}
