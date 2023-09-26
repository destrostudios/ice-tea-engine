package com.destrostudios.icetea.core.object;

import com.destrostudios.icetea.core.Application;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

public class MultiConsumableNativeObject<C> extends NativeObject {

    public MultiConsumableNativeObject() {
        consumers = new HashSet<>();
        initializedConsumers = new HashSet<>();
    }
    @Getter
    private Set<C> consumers;
    @Getter
    private Set<C> initializedConsumers;

    public void addConsumer(C consumer) {
        consumers.add(consumer);
    }

    public void removeConsumer(C consumer) {
        consumers.remove(consumer);
    }

    public void updateNative(Application application, C consumer) {
        initializedConsumers.add(consumer);
        updateNative(application);
    }

    public void onConsumerCleanup(C consumer) {
        initializedConsumers.remove(consumer);
        if (initializedConsumers.isEmpty()) {
            cleanupNative();
        }
    }
}
