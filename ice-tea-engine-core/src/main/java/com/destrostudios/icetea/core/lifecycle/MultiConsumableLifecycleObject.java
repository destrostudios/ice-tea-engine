package com.destrostudios.icetea.core.lifecycle;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

public class MultiConsumableLifecycleObject<C> extends LifecycleObject {

    public MultiConsumableLifecycleObject() {
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

    public void onConsumerInit(C consumer) {
        initializedConsumers.add(consumer);
    }

    public void onConsumerCleanup(C consumer) {
        initializedConsumers.remove(consumer);
        if (initializedConsumers.isEmpty()) {
            cleanup();
        }
    }
}
