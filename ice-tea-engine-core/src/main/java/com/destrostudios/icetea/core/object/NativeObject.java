package com.destrostudios.icetea.core.object;

import com.destrostudios.icetea.core.Application;

public class NativeObject {

    protected Application application;

    public final void updateNative(Application application) {
        if (this.application == null) {
            this.application = application;
            initNative();
        }
        updateNative();
    }

    protected void initNative() {

    }

    protected void updateNative() {

    }

    public final void cleanupNative() {
        if (application != null) {
            cleanupNativeInternal();
            application = null;
        }
    }

    protected void cleanupNativeInternal() {

    }
}
