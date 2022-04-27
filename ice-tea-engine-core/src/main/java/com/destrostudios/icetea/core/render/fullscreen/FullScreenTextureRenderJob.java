package com.destrostudios.icetea.core.render.fullscreen;

import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.texture.Texture;

public class FullScreenTextureRenderJob extends FullScreenQuadRenderJob {

    public FullScreenTextureRenderJob(Texture texture) {
        this.texture = texture;
    }
    private Texture texture;

    @Override
    protected void initResourceDescriptorSet() {
        super.initResourceDescriptorSet();
        resourceDescriptorSet.setDescriptor("colorMap", texture.getDescriptor("default"));
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        application.getSwapChain().setResourceActive(texture);
    }

    @Override
    public Shader getFragmentShader() {
        return new Shader("com/destrostudios/icetea/core/shaders/fullScreenTexture.frag");
    }
}
