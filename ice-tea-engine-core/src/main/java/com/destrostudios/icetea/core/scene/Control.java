package com.destrostudios.icetea.core.scene;

import com.destrostudios.icetea.core.Application;

public class Control {

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
            onSpatialChanged();
        }
        setActive(true);
    }

    protected void onSpatialChanged() {

    }

    public void update(float tpf) {

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
}
