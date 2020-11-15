package com.destrostudios.icetea.core;

import lombok.Getter;

public class SceneGraph {

    public SceneGraph(Application application) {
        this.application = application;
        rootNode = new Node();
    }
    private Application application;
    @Getter
    private Node rootNode;

    public void update() {
        if (rootNode.update(application)) {
            application.getSwapChain().recreateCommandBuffers();
        }
    }
}
