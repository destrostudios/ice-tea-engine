package com.destrostudios.icetea.core.object;

import com.destrostudios.icetea.core.Application;

public class LogicalObject {

    protected Application application;

    public void updateLogicalState(Application application, float tpf) {
        this.application = application;
    }

    public void applyLogicalState() {

    }

    public void updateNativeState(Application application) {
        this.application = application;
    }

    public final void cleanupNativeState() {
        if (application != null) {
            cleanupNativeStateInternal();
            application = null;
        }
    }

    public void cleanupNativeStateInternal() {

    }
}
