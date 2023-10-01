package com.destrostudios.icetea.core.render.bucket;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.render.bucket.comparators.BackToFrontGeometryComparator;
import com.destrostudios.icetea.core.render.bucket.comparators.FrontToBackGeometryComparator;
import com.destrostudios.icetea.core.render.bucket.comparators.ZGeometryComparator;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.scene.Spatial;
import com.destrostudios.icetea.core.util.ListUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BucketRenderer {

    public BucketRenderer(Application application) {
        this.application = application;
        buckets = new HashMap<>();
        buckets.put(RenderBucketType.BACKGROUND, new RenderBucket(new FrontToBackGeometryComparator(application.getSceneCamera())));
        buckets.put(RenderBucketType.OPAQUE, new RenderBucket(new FrontToBackGeometryComparator(application.getSceneCamera())));
        buckets.put(RenderBucketType.TRANSPARENT, new RenderBucket(new BackToFrontGeometryComparator(application.getSceneCamera())));
        buckets.put(RenderBucketType.GUI, new RenderBucket(new ZGeometryComparator(), application.getGuiCamera()));

        tmpBucketGeometries = new HashMap<>();
        buckets.keySet().forEach(bucketType -> tmpBucketGeometries.put(bucketType, new ArrayList<>()));
    }
    private Application application;
    private HashMap<RenderBucketType, RenderBucket> buckets;
    // TODO: Introduce TempVars
    private HashMap<RenderBucketType, ArrayList<Geometry>> tmpBucketGeometries;
    @Getter
    private List<List<Geometry>> splitOrderedGeometries;

    public void updateSplitOrderedGeometries() {
        application.getRootNode().forEachGeometry(geometry -> {
            RenderBucketType bucketType = getEffectiveBucketType(geometry);
            tmpBucketGeometries.get(bucketType).add(geometry);
        });
        tmpBucketGeometries.forEach((bucketType, geometries) -> {
            RenderBucket bucket = buckets.get(bucketType);
            bucket.sort(geometries);
        });
        ArrayList<Geometry> orderedGeometries = new ArrayList<>();
        orderedGeometries.addAll(tmpBucketGeometries.get(RenderBucketType.OPAQUE));
        orderedGeometries.addAll(tmpBucketGeometries.get(RenderBucketType.BACKGROUND));
        orderedGeometries.addAll(tmpBucketGeometries.get(RenderBucketType.TRANSPARENT));
        orderedGeometries.addAll(tmpBucketGeometries.get(RenderBucketType.GUI));
        tmpBucketGeometries.values().forEach(ArrayList::clear);
        splitOrderedGeometries = ListUtil.split(orderedGeometries, application.getConfig().getWorkerThreads());
    }

    public RenderBucket getBucket(Geometry geometry) {
        return buckets.get(getEffectiveBucketType(geometry));
    }

    private RenderBucketType getEffectiveBucketType(Spatial spatial) {
        if (spatial.getRenderBucket() != null) {
            return spatial.getRenderBucket();
        } else if (spatial.getParent() != null) {
            return getEffectiveBucketType(spatial.getParent());
        }
        return RenderBucketType.OPAQUE;
    }
}
