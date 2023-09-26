package com.destrostudios.icetea.core.resource;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import com.destrostudios.icetea.core.object.NativeObject;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public abstract class Resource extends NativeObject implements ContextCloneable {

    public Resource() { }

    public Resource(Resource resource, CloneContext context) {
        for (Map.Entry<String, ResourceDescriptor<?>> entry : resource.descriptors.entrySet()) {
            setDescriptor(entry.getKey(), entry.getValue().clone(context));
        }
    }
    @Getter
    private boolean outdated;
    private HashMap<String, ResourceDescriptor<?>> descriptors = new HashMap<>();

    public void setDescriptor(String key, ResourceDescriptor descriptor) {
        descriptor.setResource(this);
        descriptors.put(key, descriptor);
    }

    @Override
    public void updateNative() {
        super.updateNative();
        updateResource();
        for (ResourceDescriptor<?> resourceDescriptor : descriptors.values()) {
            resourceDescriptor.updateNative(application);
        }
        outdated = false;
    }

    protected abstract void updateResource();

    protected void setOutdated() {
        outdated = true;
    }

    public ResourceDescriptor<?> getDescriptor(String key) {
        return descriptors.get(key);
    }

    @Override
    protected void cleanupNativeInternal() {
        for (ResourceDescriptor<?> resourceDescriptor : descriptors.values()) {
            resourceDescriptor.cleanupNative();
        }
        super.cleanupNativeInternal();
    }

    @Override
    public abstract Resource clone(CloneContext context);
}
