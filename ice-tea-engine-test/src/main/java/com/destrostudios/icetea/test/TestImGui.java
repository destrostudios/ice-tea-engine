package com.destrostudios.icetea.test;

import com.destrostudios.icetea.core.AppSystem;
import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.input.Event;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.destrostudios.icetea.imgui.ImGuiSystem;
import imgui.ImGui;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_INSERT;

public class TestImGui extends Application {

    public static void main(String[] args) {
        new TestImGui().start();
    }

    public TestImGui() {
        config.setEnableValidationLayer(true);
        config.setDisplayFpsInTitle(true);
    }

    @Override
    protected void init() {
        super.init();
        ImGuiSystem imGuiSystem = new ImGuiSystem(ImGui::showDemoWindow);
        addSystem(imGuiSystem);
        addSystem(new AppSystem() {

            @Override
            public void onAttached() {
                super.onAttached();
                inputManager.addMouseScrollListener(this::onEvent);
                inputManager.addMouseButtonListener(this::onEvent);
                inputManager.addKeyListener(this::onEvent);
                inputManager.addCharacterListener(this::onEvent);
            }

            private void onEvent(Event event) {
                System.out.println("Event: " + event.getClass().getSimpleName());
                if (event instanceof KeyEvent keyEvent) {
                    if (keyEvent.getKey() == GLFW_KEY_INSERT) {
                        if (keyEvent.getAction() == GLFW_PRESS) {
                            if (hasSystem(imGuiSystem)) {
                                removeSystem(imGuiSystem);
                            } else {
                                addSystem(imGuiSystem);
                            }
                        }
                    }
                }
            }
        });
    }
}
