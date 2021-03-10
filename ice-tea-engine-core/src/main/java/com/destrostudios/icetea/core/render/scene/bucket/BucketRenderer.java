package com.destrostudios.icetea.core.render.scene.bucket;

import com.destrostudios.icetea.core.render.scene.bucket.comparators.BackToFrontGeometryComparator;
import com.destrostudios.icetea.core.render.scene.bucket.comparators.FrontToBackGeometryComparator;
import com.destrostudios.icetea.core.render.scene.bucket.comparators.ZGeometryComparator;
import com.destrostudios.icetea.core.scene.Camera;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.scene.Node;
import com.destrostudios.icetea.core.scene.Spatial;

import java.util.HashMap;
import java.util.function.Consumer;

public class BucketRenderer {

    public BucketRenderer(Camera camera) {
        buckets = new HashMap<>();
        buckets.put(RenderBucketType.BACKGROUND, new RenderBucket(new FrontToBackGeometryComparator(camera)));
        buckets.put(RenderBucketType.OPAQUE, new RenderBucket(new FrontToBackGeometryComparator(camera)));
        buckets.put(RenderBucketType.TRANSPARENT, new RenderBucket(new BackToFrontGeometryComparator(camera)));
        buckets.put(RenderBucketType.GUI, new RenderBucket(new ZGeometryComparator()));
    }
    private HashMap<RenderBucketType, RenderBucket> buckets;

    public void render(Node node, Consumer<Geometry> renderGeometry) {
        node.forEachGeometry(geometry -> {
            RenderBucket bucket = buckets.get(getEffectiveRenderBucketType(geometry));
            bucket.add(geometry);
        });
        for (RenderBucket bucket : buckets.values()) {
            bucket.sort();
        }
        renderBucket(RenderBucketType.OPAQUE, renderGeometry);
        renderBucket(RenderBucketType.BACKGROUND, renderGeometry);
        renderBucket(RenderBucketType.TRANSPARENT, renderGeometry);
        renderBucket(RenderBucketType.GUI, renderGeometry);
        for (RenderBucket bucket : buckets.values()) {
            bucket.clear();
        }
    }

    public RenderBucketType getEffectiveRenderBucketType(Spatial spatial) {
        if (spatial.getRenderBucket() != null) {
            return spatial.getRenderBucket();
        } else if (spatial.getParent() != null) {
            return getEffectiveRenderBucketType(spatial.getParent());
        }
        return RenderBucketType.OPAQUE;
    }

    private void renderBucket(RenderBucketType renderBucketType, Consumer<Geometry> renderGeometry) {
        RenderBucket bucket = buckets.get(renderBucketType);
        bucket.forEach(renderGeometry);
    }
}
