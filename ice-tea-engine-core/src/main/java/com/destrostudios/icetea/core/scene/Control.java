package com.destrostudios.icetea.core.scene;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;

public abstract class Control implements ContextCloneable {

    protected Control() { }

    protected Control(Control control) {
        active = control.active;
    }
    protected Application application;
    protected Spatial spatial;
    protected boolean active;

    public boolean isInitialized() {
        return (application != null);
    }

    public void init(Application application) {
        this.application = application;
    }

    public void setSpatial(Spatial spatial) {
        if (spatial != this.spatial) {
            this.spatial = spatial;
            onAdd();
        }
        setActive(true);
    }

    protected void onAdd() {

    }

    public void update(float tpf) {

    }

    public void updateUniformBuffers(int currentImage) {

    }

    public void onRemove() {
        spatial = null;
    }

    public void onRemoveFromRoot() {
        setActive(false);
    }

    private void setActive(boolean active) {
        if (active != this.active) {
            this.active = active;
            onActiveChanged();
        }
    }

    protected void onActiveChanged() {

    }

    public void cleanup() {

    }

    @Override
    public abstract Control clone(CloneContext context);
}
