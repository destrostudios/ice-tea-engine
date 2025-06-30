package com.destrostudios.icetea.imgui;

import com.destrostudios.icetea.core.AppSystem;
import com.destrostudios.icetea.core.WindowResizeListener;
import com.destrostudios.icetea.core.input.*;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.resource.descriptor.SimpleTextureDescriptor;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.shader.FileShader;
import com.destrostudios.icetea.core.texture.BufferedTexture;
import com.destrostudios.icetea.core.texture.TextureData;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import imgui.flag.ImGuiBackendFlags;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiMouseCursor;
import imgui.type.ImInt;
import lombok.Getter;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.vulkan.VK10.*;

public class ImGuiSystem extends AppSystem implements WindowResizeListener, KeyListener, CharacterListener, MousePositionListener, MouseButtonListener, MouseScrollListener {

    public ImGuiSystem(Runnable updateImGuiFrame) {
        this.updateImGuiFrame = updateImGuiFrame;
    }
    private Runnable updateImGuiFrame;
    private BufferedTexture fontsTexture;
    @Getter
    private Geometry geometry;
    private final long[] mouseCursors = new long[ImGuiMouseCursor.COUNT];

    @Override
    public void onAttached() {
        super.onAttached();
        initImGui();
        fontsTexture = createFontsTexture();
        geometry = createGeometry();
        application.getGuiNode().add(geometry);
        application.addWindowResizeListener(this);
        application.getInputManager().addKeyListener(this, 0);
        application.getInputManager().addCharacterListener(this, 0);
        application.getInputManager().addMousePositionListener(this, 0);
        application.getInputManager().addMouseButtonListener(this, 0);
        application.getInputManager().addMouseScrollListener(this, 0);
    }

    private void initImGui() {
        ImGui.createContext();

        ImGuiIO imGuiIO = ImGui.getIO();
        imGuiIO.addBackendFlags(ImGuiBackendFlags.HasMouseCursors);
        imGuiIO.setIniFilename(null);
        imGuiIO.setDisplayFramebufferScale(1, 1);
        onWindowResize(application.getConfig().getWidth(), application.getConfig().getHeight());

        // Special keys
        int[] keyMap = new int[ImGuiKey.COUNT];
        keyMap[ImGuiKey.Tab] = GLFW_KEY_TAB;
        keyMap[ImGuiKey.LeftArrow] = GLFW_KEY_LEFT;
        keyMap[ImGuiKey.RightArrow] = GLFW_KEY_RIGHT;
        keyMap[ImGuiKey.UpArrow] = GLFW_KEY_UP;
        keyMap[ImGuiKey.DownArrow] = GLFW_KEY_DOWN;
        keyMap[ImGuiKey.PageUp] = GLFW_KEY_PAGE_UP;
        keyMap[ImGuiKey.PageDown] = GLFW_KEY_PAGE_DOWN;
        keyMap[ImGuiKey.Home] = GLFW_KEY_HOME;
        keyMap[ImGuiKey.End] = GLFW_KEY_END;
        keyMap[ImGuiKey.Insert] = GLFW_KEY_INSERT;
        keyMap[ImGuiKey.Delete] = GLFW_KEY_DELETE;
        keyMap[ImGuiKey.Backspace] = GLFW_KEY_BACKSPACE;
        keyMap[ImGuiKey.Space] = GLFW_KEY_SPACE;
        keyMap[ImGuiKey.Enter] = GLFW_KEY_ENTER;
        keyMap[ImGuiKey.Escape] = GLFW_KEY_ESCAPE;
        keyMap[ImGuiKey.KeyPadEnter] = GLFW_KEY_KP_ENTER;
        keyMap[ImGuiKey.A] = GLFW_KEY_A;
        keyMap[ImGuiKey.C] = GLFW_KEY_C;
        keyMap[ImGuiKey.V] = GLFW_KEY_V;
        keyMap[ImGuiKey.X] = GLFW_KEY_X;
        keyMap[ImGuiKey.Y] = GLFW_KEY_Y;
        keyMap[ImGuiKey.Z] = GLFW_KEY_Z;
        imGuiIO.setKeyMap(keyMap);

        // Clipboard
        imGuiIO.setGetClipboardTextFn(new ImStrSupplier() {

            @Override
            public String get() {
                String text = glfwGetClipboardString(application.getWindow());
                return ((text != null) ? text : "");
            }
        });
        imGuiIO.setSetClipboardTextFn(new ImStrConsumer() {

            @Override
            public void accept(String text) {
                glfwSetClipboardString(application.getWindow(), text);
            }
        });

        // Mouse cursors
        mouseCursors[ImGuiMouseCursor.Arrow] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        mouseCursors[ImGuiMouseCursor.TextInput] = glfwCreateStandardCursor(GLFW_IBEAM_CURSOR);
        mouseCursors[ImGuiMouseCursor.ResizeAll] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        mouseCursors[ImGuiMouseCursor.ResizeNS] = glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR);
        mouseCursors[ImGuiMouseCursor.ResizeEW] = glfwCreateStandardCursor(GLFW_HRESIZE_CURSOR);
        mouseCursors[ImGuiMouseCursor.ResizeNESW] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        mouseCursors[ImGuiMouseCursor.ResizeNWSE] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        mouseCursors[ImGuiMouseCursor.Hand] = glfwCreateStandardCursor(GLFW_HAND_CURSOR);
        mouseCursors[ImGuiMouseCursor.NotAllowed] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
    }

    private BufferedTexture createFontsTexture() {
        ImInt width = new ImInt();
        ImInt height = new ImInt();
        ByteBuffer pixels = ImGui.getIO().getFonts().getTexDataAsRGBA32(width, height);
        BufferedTexture fontsTexture = new BufferedTexture(
            () -> new TextureData(pixels, width.get(), height.get(), () -> {}),
            VK_FORMAT_R8G8B8A8_SRGB,
            VK_IMAGE_USAGE_SAMPLED_BIT,
            VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL
        );
        fontsTexture.setDescriptor("default", new SimpleTextureDescriptor());
        return fontsTexture;
    }

    private Geometry createGeometry() {
        Material material = new Material();
        material.setVertexShader(new FileShader("com/destrostudios/icetea/imgui/shaders/imgui.vert"));
        material.setFragmentShader(new FileShader("com/destrostudios/icetea/imgui/shaders/imgui.frag"));
        material.setTexture("diffuseMap", fontsTexture);
        material.setCullMode(VK_CULL_MODE_NONE);
        material.setDepthTest(false);
        material.setDepthWrite(false);
        material.setTransparent(true);

        Geometry geometry = new Geometry();
        geometry.setMesh(new ImGuiMesh());
        geometry.setMaterial(material);
        geometry.setRenderer(new ImGuiGeometryRenderer());
        return geometry;
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        renderImGui();
        updateMouseCursor();
        updateGeometry();
    }

    private void renderImGui() {
        ImGui.newFrame();
        updateImGuiFrame.run();
        ImGui.endFrame();
        ImGui.render();
    }

    private void updateMouseCursor() {
        ImGuiIO imGuiIO = ImGui.getIO();
        boolean noCursorChange = imGuiIO.hasConfigFlags(ImGuiConfigFlags.NoMouseCursorChange);
        boolean isCursorDisabled = glfwGetInputMode(application.getWindow(), GLFW_CURSOR) == GLFW_CURSOR_DISABLED;
        if (noCursorChange || isCursorDisabled) {
            return;
        }
        int mouseCursor = ImGui.getMouseCursor();
        if (mouseCursor == ImGuiMouseCursor.None) {
            glfwSetInputMode(application.getWindow(), GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
        } else {
            glfwSetInputMode(application.getWindow(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            glfwSetCursor(application.getWindow(), mouseCursors[mouseCursor]);
        }
    }

    private void updateGeometry() {
        ((ImGuiMesh) geometry.getMesh()).update(ImGui.getDrawData());
    }

    @Override
    public void onWindowResize(int width, int height) {
        ImGui.getIO().setDisplaySize(width, height);
    }

    @Override
    public void onKeyEvent(KeyEvent keyEvent) {
        ImGuiIO imGuiIO = ImGui.getIO();
        if (keyEvent.getAction() == GLFW_PRESS) {
            imGuiIO.setKeysDown(keyEvent.getKey(), true);
        } else if (keyEvent.getAction() == GLFW_RELEASE) {
            imGuiIO.setKeysDown(keyEvent.getKey(), false);
        }
        imGuiIO.setKeyCtrl(imGuiIO.getKeysDown(GLFW_KEY_LEFT_CONTROL) || imGuiIO.getKeysDown(GLFW_KEY_RIGHT_CONTROL));
        imGuiIO.setKeyShift(imGuiIO.getKeysDown(GLFW_KEY_LEFT_SHIFT) || imGuiIO.getKeysDown(GLFW_KEY_RIGHT_SHIFT));
        imGuiIO.setKeyAlt(imGuiIO.getKeysDown(GLFW_KEY_LEFT_ALT) || imGuiIO.getKeysDown(GLFW_KEY_RIGHT_ALT));
        imGuiIO.setKeySuper(imGuiIO.getKeysDown(GLFW_KEY_LEFT_SUPER) || imGuiIO.getKeysDown(GLFW_KEY_RIGHT_SUPER));
        if (imGuiIO.getWantCaptureKeyboard()) {
            keyEvent.stopPropagating();
        }
    }

    @Override
    public void onCharacterEvent(CharacterEvent characterEvent) {
        ImGuiIO imGuiIO = ImGui.getIO();
        imGuiIO.addInputCharacter(characterEvent.getCodepoint());
        if (imGuiIO.getWantCaptureKeyboard()) {
            characterEvent.stopPropagating();
        }
    }

    @Override
    public void onMousePositionEvent(MousePositionEvent mousePositionEvent) {
        ImGui.getIO().setMousePos((float) mousePositionEvent.getX(), (float) mousePositionEvent.getY());
    }

    @Override
    public void onMouseButtonEvent(MouseButtonEvent mouseButtonEvent) {
        ImGuiIO imGuiIO = ImGui.getIO();
        imGuiIO.setMouseDown(mouseButtonEvent.getButton(), (mouseButtonEvent.getAction() == GLFW_PRESS));
        if (imGuiIO.getWantCaptureMouse()) {
            mouseButtonEvent.stopPropagating();
        }
    }

    @Override
    public void onMouseScrollEvent(MouseScrollEvent mouseScrollEvent) {
        ImGuiIO imGuiIO = ImGui.getIO();
        imGuiIO.setMouseWheelH(imGuiIO.getMouseWheelH() + (float) mouseScrollEvent.getXOffset());
        imGuiIO.setMouseWheel(imGuiIO.getMouseWheel() + (float) mouseScrollEvent.getYOffset());
        if (imGuiIO.getWantCaptureMouse()) {
            mouseScrollEvent.stopPropagating();
        }
    }

    @Override
    public void onDetached() {
        super.onDetached();
        application.getInputManager().removeKeyListener(this);
        application.getInputManager().removeCharacterListener(this);
        application.getInputManager().removeMousePositionListener(this);
        application.getInputManager().removeMouseButtonListener(this);
        application.getInputManager().removeMouseScrollListener(this);
        application.removeWindowResizeListener(this);
        application.getGuiNode().remove(geometry);
        geometry.cleanupNativeState();
        fontsTexture.cleanupNative();
        ImGui.destroyContext();
        for (long mouseCursor : mouseCursors) {
            glfwDestroyCursor(mouseCursor);
        }
    }
}
