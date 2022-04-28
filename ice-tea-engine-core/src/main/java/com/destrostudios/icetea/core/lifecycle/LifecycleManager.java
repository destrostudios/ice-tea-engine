package com.destrostudios.icetea.core.lifecycle;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class LifecycleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleManager.class);

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

    public void cleanupInactiveObjects() {
        for (LifecycleObject lifecycleObject : getInactiveObjects().toArray(new LifecycleObject[0])) {
            lifecycleObject.cleanup();
        }
        int newInactiveObjectsCount = getInactiveObjects().size();
        if (newInactiveObjectsCount > 0) {
            LOGGER.warn("There are still {} inactive lifecycle objects after cleaning up all previously inactive ones.", newInactiveObjectsCount);
        }
    }

    public List<LifecycleObject> getInactiveObjects() {
        return initializedObjects.stream()
                .filter(lifecycleObject -> !activeObjects.contains(lifecycleObject))
                .collect(Collectors.toList());
    }
}
