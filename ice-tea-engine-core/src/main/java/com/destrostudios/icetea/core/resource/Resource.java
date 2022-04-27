package com.destrostudios.icetea.core.resource;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public abstract class Resource extends LifecycleObject implements ContextCloneable {

    public Resource() { }

    public Resource(Resource resource, CloneContext context) {
        for (Map.Entry<String, ResourceDescriptor<?>> entry : resource.descriptors.entrySet()) {
            setDescriptor(entry.getKey(), entry.getValue().clone(context));
        }
    }
    @Getter
    private boolean wasOutdated;
    private HashMap<String, ResourceDescriptor<?>> descriptors = new HashMap<>();

    public void setDescriptor(String key, ResourceDescriptor descriptor) {
        descriptor.setResource(this);
        descriptors.put(key, descriptor);
    }

    @Override
    protected void update(float tpf) {
        super.update(tpf);
        wasOutdated = false;
        updateResource();
        for (ResourceDescriptor<?> resourceDescriptor : descriptors.values()) {
            resourceDescriptor.update(application, tpf);
        }
    }

    protected abstract void updateResource();

    protected void setWasOutdated() {
        wasOutdated = true;
    }

    public ResourceDescriptor<?> getDescriptor(String key) {
        return descriptors.get(key);
    }

    @Override
    protected void cleanupInternal() {
        for (ResourceDescriptor<?> resourceDescriptor : descriptors.values()) {
            resourceDescriptor.cleanup();
        }
        super.cleanupInternal();
    }

    @Override
    public abstract Resource clone(CloneContext context);
}
