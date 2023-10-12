package com.destrostudios.icetea.core.render.fullscreen;

import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.texture.Texture;

public class FullScreenTextureRenderJob extends FullScreenQuadRenderJob {

    public FullScreenTextureRenderJob(Texture texture) {
        this.texture = texture;
    }
    private Texture texture;
    private Shader fragmentShader = new Shader("com/destrostudios/icetea/core/shaders/fullScreenTexture.frag");

    @Override
    protected void initResourceDescriptorSet() {
        super.initResourceDescriptorSet();
        resourceDescriptorSet.setDescriptor("colorMap", texture.getDescriptor("default"));
    }

    @Override
    public void updateNative() {
        super.updateNative();
        texture.updateNative(application);
    }

    @Override
    public Shader getFragmentShader() {
        return fragmentShader;
    }
}
