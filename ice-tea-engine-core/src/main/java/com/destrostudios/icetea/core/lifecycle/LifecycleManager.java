package com.destrostudios.icetea.core.lifecycle;

import lombok.Getter;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class LifecycleManager {

    @Getter
    private LinkedList<LifecycleObject> initializedObjects = new LinkedList<>();
    @Getter
    private HashSet<LifecycleObject> activeObjects = new HashSet<>();

    public void onNewCycle() {
        activeObjects.clear();
    }

    void onInit(LifecycleObject lifecycleObject) {
        initializedObjects.add(lifecycleObject);
    }

    void onUpdate(LifecycleObject lifecycleObject) {
        boolean wasInactive = activeObjects.add(lifecycleObject);
        if (!wasInactive) {
            // TODO: Decide if this should be enabled when material and mesh are only updated once per frame
            // LOGGER.warn("This lifecycle object {} was already updated in this frame.", lifecycleObject);
        }
    }

    void onCleanup(LifecycleObject lifecycleObject) {
        initializedObjects.remove(lifecycleObject);
    }

    public List<LifecycleObject> getInactiveObjects() {
        return initializedObjects.stream()
                .filter(lifecycleObject -> !activeObjects.contains(lifecycleObject))
                .collect(Collectors.toList());
    }
}
