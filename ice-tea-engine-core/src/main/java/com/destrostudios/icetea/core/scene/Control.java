package com.destrostudios.icetea.core.scene;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import com.destrostudios.icetea.core.object.LogicalObject;
import lombok.Getter;

public abstract class Control extends LogicalObject implements ContextCloneable {

    protected Control() { }

    @Getter
    protected Spatial spatial;
    private Spatial spatialToSet;

    public void setSpatial(Spatial spatial) {
        this.spatialToSet = spatial;
        if (spatial == null) {
            checkSpatialChange();
        }
    }

    @Override
    public void updateLogicalState(Application application, float tpf) {
        super.updateLogicalState(application, tpf);
        checkSpatialChange();
    }

    private void checkSpatialChange() {
        if ((spatialToSet != spatial) && (application != null)) {
            if (spatial != null) {
                onDetached();
            }
            spatial = spatialToSet;
            if (spatial != null) {
                onAttached();
            }
        }
    }

    protected void onAttached() {

    }

    protected void onDetached() {

    }

    @Override
    public abstract Control clone(CloneContext context);
}
