package com.destrostudios.icetea.core.clone;

import lombok.Getter;

import java.util.HashMap;

public class CloneContext {

    public static CloneContext reuseAll() {
        return new CloneContext(false, false);
    }

    public static CloneContext cloneMeshesAndMaterials() {
        return new CloneContext(false, true);
    }

    public static CloneContext cloneOnlyMeshes() {
        return new CloneContext(true, false);
    }

    public static CloneContext cloneOnlyMaterials() {
        return new CloneContext(true, true);
    }

    private CloneContext(boolean cloneMeshes, boolean cloneMaterials) {
        this.cloneMeshes = cloneMeshes;
        this.cloneMaterials = cloneMaterials;
        cloneCache = new HashMap<>();
    }
    @Getter
    private boolean cloneMeshes;
    @Getter
    private boolean cloneMaterials;
    private HashMap<ContextCloneable, Object> cloneCache;

    public <T extends ContextCloneable> T cloneByReference(T object) {
        if (object == null) {
            return null;
        }
        Object clone = cloneCache.get(object);
        if (clone == null) {
            clone = object.clone(this);
            cloneCache.put(object, clone);
        }
        return (T) clone;
    }
}
