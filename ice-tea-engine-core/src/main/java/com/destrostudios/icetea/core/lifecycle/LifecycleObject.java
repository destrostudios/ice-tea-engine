package com.destrostudios.icetea.core.lifecycle;

import com.destrostudios.icetea.core.Application;

public class LifecycleObject {

    protected Application application;

    public void update(Application application, int imageIndex, float tpf) {
        if (this.application == null) {
            init(application);
        }
    }

    protected void init(Application application) {
        this.application = application;
    }

    protected boolean isInitialized() {
        return (application != null);
    }

    public void cleanup() {
        application = null;
    }
}
