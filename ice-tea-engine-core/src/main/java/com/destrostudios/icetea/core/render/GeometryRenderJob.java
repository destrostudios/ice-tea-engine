package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.scene.Geometry;

import java.util.HashMap;
import java.util.Map;

public abstract class GeometryRenderJob<GRC extends GeometryRenderContext<?>, RPC extends RenderPipelineCreator<?, ?>> extends MultisampleRenderJob<RPC> {

    public GeometryRenderJob(String name) {
        super(name);
    }
    private HashMap<Geometry, GRC> renderContexts = new HashMap<>();

    @Override
    public void updateNative() {
        super.updateNative();
        synchronizeRenderContextsWithRootNode();
    }

    private void synchronizeRenderContextsWithRootNode() {
        // TODO: Performance can be improved here
        for (Map.Entry<Geometry, GRC> entry : renderContexts.entrySet().toArray(Map.Entry[]::new)) {
            Geometry geometry = entry.getKey();
            if ((!geometry.hasParent(application.getRootNode())) || (!isRendering(geometry))) {
                entry.getValue().cleanupNative();
                renderContexts.remove(geometry);
            }
        }
        application.getRootNode().forEachGeometry(geometry -> {
            GRC renderContext = getRenderContext(geometry);
            if ((renderContext == null) && isRendering(geometry)) {
                renderContext = createGeometryRenderContext(geometry);
                renderContexts.put(geometry, renderContext);
            }
        });
    }

    @Override
    public void afterAllRenderJobsUpdatedNative() {
        super.afterAllRenderJobsUpdatedNative();
        // Update the render contexts after the render jobs, as they rely on initialized resources from the render jobs
        for (GRC renderContext : renderContexts.values()) {
            renderContext.updateNative(application);
        }
    }

    protected abstract boolean isRendering(Geometry geometry);

    protected abstract GRC createGeometryRenderContext(Geometry geometry);

    @Override
    protected void cleanupNativeInternal() {
        for (GRC renderContext : renderContexts.values()) {
            renderContext.cleanupNative();
        }
        super.cleanupNativeInternal();
    }

    protected GRC getRenderContext(Geometry geometry) {
        return renderContexts.get(geometry);
    }
}
