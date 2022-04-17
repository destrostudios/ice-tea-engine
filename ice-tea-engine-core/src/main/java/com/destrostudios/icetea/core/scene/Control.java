package com.destrostudios.icetea.core.scene;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import lombok.Getter;

public abstract class Control extends LifecycleObject implements ContextCloneable {

    protected Control() { }

    protected Control(Control control) {
        active = control.active;
    }
    @Getter
    protected Spatial spatial;
    protected boolean active;

    public void setSpatial(Spatial spatial) {
        if (spatial != this.spatial) {
            if (spatial != null) {
                if (isInitialized()) {
                    onAdd();
                    setActive(true);
                }
            } else {
                onRemove();
                setActive(false);
            }
            this.spatial = spatial;
        }
        if (isInitialized()) {
            setActive(true);
        }
    }

    @Override
    protected void init(Application application) {
        super.init(application);
        initControl();
        if (spatial != null) {
            onAdd();
            setActive(true);
        }
    }

    protected void initControl() {

    }

    protected void onAdd() {

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

    @Override
    public abstract Control clone(CloneContext context);
}
