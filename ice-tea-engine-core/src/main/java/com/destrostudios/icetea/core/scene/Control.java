package com.destrostudios.icetea.core.scene;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import lombok.Getter;

public abstract class Control extends LifecycleObject implements ContextCloneable {

    protected Control() { }

    @Getter
    protected Spatial spatial;

    public void setSpatial(Spatial spatial) {
        if (spatial != this.spatial) {
            if (spatial != null) {
                if (isInitialized()) {
                    onAdd();
                }
            } else {
                onRemove();
            }
            this.spatial = spatial;
        }
    }

    @Override
    protected void init(Application application) {
        super.init(application);
        initControl();
        if (spatial != null) {
            onAdd();
        }
    }

    protected void initControl() {

    }

    protected void onAdd() {

    }

    protected void onRemove() {

    }

    @Override
    public abstract Control clone(CloneContext context);
}
