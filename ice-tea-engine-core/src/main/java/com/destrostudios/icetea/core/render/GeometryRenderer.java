package com.destrostudios.icetea.core.render;

import lombok.Getter;
import org.lwjgl.system.MemoryStack;

public class GeometryRenderer extends MeshRenderer {

    @Getter
    protected int[] dynamicStates;

    public <RJ extends RenderJob<?>> void drawGeometry(RenderRecorder recorder, GeometryRenderContext<RJ> geometryRenderContext, MemoryStack stack) {
        recorder.bindPipeline(geometryRenderContext.getRenderPipeline());
        recorder.bindDescriptorSets(geometryRenderContext.getResourceDescriptorSet(), stack);
        drawMesh(recorder, geometryRenderContext.getGeometry().getMesh(), stack);
    }
}
