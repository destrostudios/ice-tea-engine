package com.destrostudios.icetea.core;

import com.destrostudios.icetea.core.asset.AssetManager;
import com.destrostudios.icetea.core.asset.locator.ClasspathLocator;
import com.destrostudios.icetea.core.camera.GuiCamera;
import com.destrostudios.icetea.core.camera.SceneCamera;
import com.destrostudios.icetea.core.camera.projections.PerspectiveProjection;
import com.destrostudios.icetea.core.command.CommandPool;
import com.destrostudios.icetea.core.filter.Filter;
import com.destrostudios.icetea.core.input.*;
import com.destrostudios.icetea.core.render.bucket.BucketRenderer;
import com.destrostudios.icetea.core.light.Light;
import com.destrostudios.icetea.core.render.bucket.RenderBucketType;
import com.destrostudios.icetea.core.scene.Node;
import com.destrostudios.icetea.core.shader.ShaderManager;
import com.destrostudios.icetea.core.util.BufferUtil;
import com.destrostudios.icetea.core.util.MathUtil;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.LongBuffer;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.KHRCreateRenderpass2.VK_KHR_CREATE_RENDERPASS_2_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRDepthStencilResolve.VK_KHR_DEPTH_STENCIL_RESOLVE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRGetPhysicalDeviceProperties2.VK_KHR_GET_PHYSICAL_DEVICE_PROPERTIES_2_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRMaintenance2.VK_KHR_MAINTENANCE2_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRMultiview.VK_KHR_MULTIVIEW_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

public abstract class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    static final Set<String> DEVICE_EXTENSIONS_NAMES = Stream.of(
        // Required to use swapchains
        VK_KHR_SWAPCHAIN_EXTENSION_NAME,
        // Required to resolve multisampled depth buffers (for postprocessing)
        VK_KHR_MULTIVIEW_EXTENSION_NAME,
        VK_KHR_MAINTENANCE2_EXTENSION_NAME,
        VK_KHR_CREATE_RENDERPASS_2_EXTENSION_NAME,
        VK_KHR_DEPTH_STENCIL_RESOLVE_EXTENSION_NAME
    ).collect(toSet());

    public Application() {
        config = new ApplicationConfig();
    }
    @Getter
    protected ApplicationConfig config;

    @Getter
    private PhysicalDeviceManager physicalDeviceManager;
    @Getter
    private MemoryManager memoryManager;
    @Getter
    private ImageManager imageManager;
    @Getter
    protected InputManager inputManager;
    @Getter
    protected AssetManager assetManager;
    @Getter
    protected ShaderManager shaderManager;
    @Getter
    protected PipelineManager pipelineManager;

    @Getter
    private VkInstance instance;
    private Long debugMessenger;
    @Getter
    private long window;
    @Getter
    private long surface;
    @Getter
    private PhysicalDeviceInformation physicalDeviceInformation;
    @Getter
    private VkPhysicalDevice physicalDevice;
    @Getter
    private int msaaSamples;
    @Getter
    private VkDevice logicalDevice;
    @Getter
    private VkQueue graphicsQueue;
    @Getter
    private VkQueue presentQueue;
    @Getter
    private CommandPool commandPool;
    @Getter
    private BucketRenderer bucketRenderer;
    @Getter
    private SwapChain swapChain;
    private boolean isInitialized;
    protected float time;
    private boolean stop;

    @Getter
    protected SceneCamera sceneCamera;
    @Getter
    protected GuiCamera guiCamera;
    @Getter
    protected Node rootNode;
    @Getter
    protected Node sceneNode;
    @Getter
    protected Node guiNode;
    @Getter
    private Light light;
    @Getter
    private List<Filter> filters;
    @Getter
    private LinkedList<AppSystem> systems;
    @Getter
    private List<WindowResizeListener> windowResizeListeners;

    public void start() {
        create();
        mainLoop();
        cleanupAndClose();
    }

    private void create() {
        LOGGER.debug("Creating application...");
        physicalDeviceManager = new PhysicalDeviceManager(this);
        imageManager = new ImageManager(this);
        inputManager = new InputManager(this);
        assetManager = new AssetManager();
        assetManager.addLocator(new ClasspathLocator());
        shaderManager = new ShaderManager(assetManager);
        pipelineManager = new PipelineManager();
        rootNode = new Node();
        sceneNode = new Node();
        rootNode.add(sceneNode);
        guiNode = new Node();
        guiNode.setRenderBucket(RenderBucketType.GUI);
        rootNode.add(guiNode);
        filters = new LinkedList<>();
        systems = new LinkedList<>();
        windowResizeListeners = new LinkedList<>();
        initWindow();
        createInstance();
        initSurface();
        initPhysicalDevice();
        initLogicalDevice();
        initMemoryManager();
        initSceneCamera();
        initGuiCamera();
        commandPool = new CommandPool(this);
        commandPool.updateNative(this);
        bucketRenderer = new BucketRenderer(this);
        swapChain = new SwapChain();
        swapChain.updateNative(this);
        LOGGER.debug("Created application.");
    }

    private void initWindow() {
        LOGGER.debug("Initializing GLFW...");
        if (!glfwInit()) {
            throw new RuntimeException("Cannot initialize GLFW");
        }
        LOGGER.debug("Initialized GLFW.");

        LOGGER.debug("Setting window hint...");
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        LOGGER.debug("Set window hint.");

        LOGGER.debug("Creating window...");
        window = glfwCreateWindow(config.getWidth(), config.getHeight(), config.getTitle(), NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Cannot create window");
        }
        LOGGER.debug("Created window.");

        LOGGER.debug("Registering GLFW callbacks...");
        glfwSetFramebufferSizeCallback(window, this::onWindowResized);
        inputManager.registerCallbacks();
        LOGGER.debug("Registered GLFW callbacks.");
    }

    private void onWindowResized(long window, int width, int height) {
        LOGGER.debug("Window resized to {} x {}.", width, height);
        config.setWidth(width);
        config.setHeight(height);
        windowResizeListeners.forEach(windowResizeListener -> windowResizeListener.onWindowResize(width, height));
    }

    private void createInstance() {
        try (MemoryStack stack = stackPush()) {
            LOGGER.debug("Creating instance...");
            VkInstanceCreateInfo instanceCreateInfo = VkInstanceCreateInfo.callocStack(stack);
            instanceCreateInfo.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
            VkApplicationInfo applicationInfo = VkApplicationInfo.callocStack(stack);
            applicationInfo.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO);
            applicationInfo.pApplicationName(stack.UTF8Safe("Hello Triangle"));
            applicationInfo.applicationVersion(VK_MAKE_VERSION(1, 0, 0));
            applicationInfo.pEngineName(stack.UTF8Safe("No Engine"));
            applicationInfo.engineVersion(VK_MAKE_VERSION(1, 0, 0));
            applicationInfo.apiVersion(VK_API_VERSION_1_0);
            instanceCreateInfo.pApplicationInfo(applicationInfo);
            instanceCreateInfo.ppEnabledExtensionNames(getRequiredExtensions(stack));

            HashSet<String> enabledLayerNames = new HashSet<>();
            if (config.isEnableValidationLayer()) {
                enabledLayerNames.add("VK_LAYER_KHRONOS_validation");
                VkDebugUtilsMessengerCreateInfoEXT debugMessengerCreateInfo = createDebugMessengerCreateInfo(stack);
                instanceCreateInfo.pNext(debugMessengerCreateInfo.address());
            }
            if (config.isDisplayFpsInTitle()) {
                enabledLayerNames.add("VK_LAYER_LUNARG_monitor");
            }
            if (enabledLayerNames.size() > 0) {
                PointerBuffer enabledLayerNamesBuffer = stack.mallocPointer(enabledLayerNames.size());
                enabledLayerNames.stream()
                        .map(stack::UTF8)
                        .forEach(enabledLayerNamesBuffer::put);
                enabledLayerNamesBuffer.rewind();
                instanceCreateInfo.ppEnabledLayerNames(enabledLayerNamesBuffer);
            }
            LOGGER.debug("Enabled layers: {}", String.join(", ", enabledLayerNames));

            PointerBuffer instancePointer = stack.mallocPointer(1);
            int result = vkCreateInstance(instanceCreateInfo, null, instancePointer);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create instance (result = " + result + ")");
            }
            instance = new VkInstance(instancePointer.get(0), instanceCreateInfo);
            LOGGER.debug("Created instance.");

            if (config.isEnableValidationLayer()) {
                LOGGER.debug("Creating debug utils messenger.");
                VkDebugUtilsMessengerCreateInfoEXT debugMessengerCreateInfo = createDebugMessengerCreateInfo(stack);
                LongBuffer pDebugMessenger = stack.longs(VK_NULL_HANDLE);
                result = vkCreateDebugUtilsMessengerEXT(instance, debugMessengerCreateInfo, null, pDebugMessenger);
                if (result != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create debug messenger (result = " + result + ")");
                }
                debugMessenger = pDebugMessenger.get(0);
                LOGGER.debug("Created debug utils messenger.");
            }
        }
    }

    private PointerBuffer getRequiredExtensions(MemoryStack stack) {
        PointerBuffer glfwExtensions = glfwGetRequiredInstanceExtensions();
        int additionalExtensions = 1;
        if (config.isEnableValidationLayer()) {
            additionalExtensions++;
        }
        PointerBuffer requiredExtensions = stack.mallocPointer(glfwExtensions.capacity() + additionalExtensions);
        requiredExtensions.put(glfwExtensions);
        // Required to use the extension to resolve multisampled depth buffers (for postprocessing)
        requiredExtensions.put(stack.UTF8(VK_KHR_GET_PHYSICAL_DEVICE_PROPERTIES_2_EXTENSION_NAME));
        if (config.isEnableValidationLayer()) {
            requiredExtensions.put(stack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME));
        }
        return requiredExtensions.rewind();
    }

    private VkDebugUtilsMessengerCreateInfoEXT createDebugMessengerCreateInfo(MemoryStack stack) {
        VkDebugUtilsMessengerCreateInfoEXT debugMessengerCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.callocStack(stack);
        debugMessengerCreateInfo.sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
        debugMessengerCreateInfo.messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
        debugMessengerCreateInfo.messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
        debugMessengerCreateInfo.pfnUserCallback(this::onDebugMessengerCallback);
        return debugMessengerCreateInfo;
    }

    protected int onDebugMessengerCallback(int messageSeverity, int messageType, long pCallbackData, long pUserData) {
        VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);
        String message = callbackData.pMessageString();
        switch (messageSeverity) {
            case VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT: LOGGER.debug(message); break;
            case VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT: LOGGER.info(message); break;
            case VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT: LOGGER.warn(message); break;
            case VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT: LOGGER.error(message); break;
        }
        return VK_FALSE;
    }

    private void initSurface() {
        try (MemoryStack stack = stackPush()) {
            LOGGER.debug("Initializing surface...");
            LongBuffer pSurface = stack.longs(VK_NULL_HANDLE);
            int result = glfwCreateWindowSurface(instance, window, null, pSurface);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create window surface (result = " + result + ")");
            }
            surface = pSurface.get(0);
            LOGGER.debug("Initialized surface.");
        }
    }

    private void initPhysicalDevice() {
        LOGGER.debug("Initializing physical device...");
        physicalDeviceInformation = physicalDeviceManager.pickPhysicalDevice();
        physicalDevice = physicalDeviceInformation.getPhysicalDevice();
        msaaSamples = physicalDeviceInformation.getMaxSamples();
        LOGGER.debug("Initialized physical device.");
    }

    private void initLogicalDevice() {
        LOGGER.debug("Initializing logical device...");
        try (MemoryStack stack = stackPush()) {
            VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.callocStack(stack);
            deviceCreateInfo.sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO);

            Set<String> enabledExtensionNames = Stream.of(
                // Required to use swapchains
                VK_KHR_SWAPCHAIN_EXTENSION_NAME,
                // Required to resolve multisampled depth buffers (for postprocessing)
                VK_KHR_MULTIVIEW_EXTENSION_NAME,
                VK_KHR_MAINTENANCE2_EXTENSION_NAME,
                VK_KHR_CREATE_RENDERPASS_2_EXTENSION_NAME,
                VK_KHR_DEPTH_STENCIL_RESOLVE_EXTENSION_NAME
            ).collect(toSet());
            LOGGER.debug("Enabled extensions: {}", String.join(", ", enabledExtensionNames));
            deviceCreateInfo.ppEnabledExtensionNames(BufferUtil.asPointerBuffer(enabledExtensionNames, stack));

            int[] uniqueQueueFamilyIndices = physicalDeviceInformation.getUniqueQueueFamilyIndices();
            VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.callocStack(uniqueQueueFamilyIndices.length, stack);
            for(int i = 0; i < uniqueQueueFamilyIndices.length; i++) {
                VkDeviceQueueCreateInfo queueCreateInfo = queueCreateInfos.get(i);
                queueCreateInfo.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
                queueCreateInfo.queueFamilyIndex(uniqueQueueFamilyIndices[i]);
                queueCreateInfo.pQueuePriorities(stack.floats(1));
            }
            deviceCreateInfo.pQueueCreateInfos(queueCreateInfos);

            VkPhysicalDeviceFeatures enabledDeviceFeatures = VkPhysicalDeviceFeatures.callocStack(stack);
            enabledDeviceFeatures.samplerAnisotropy(true);
            enabledDeviceFeatures.sampleRateShading(true);
            enabledDeviceFeatures.tessellationShader(true);
            enabledDeviceFeatures.geometryShader(true);
            enabledDeviceFeatures.shaderClipDistance(true);
            enabledDeviceFeatures.fillModeNonSolid(true);
            deviceCreateInfo.pEnabledFeatures(enabledDeviceFeatures);

            PointerBuffer pDevice = stack.pointers(VK_NULL_HANDLE);
            int result = vkCreateDevice(physicalDevice, deviceCreateInfo, null, pDevice);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create logical device (result = " + result + ")");
            }
            logicalDevice = new VkDevice(pDevice.get(0), physicalDevice, deviceCreateInfo);
            LOGGER.debug("Created logical device.");

            PointerBuffer pQueue = stack.pointers(VK_NULL_HANDLE);

            vkGetDeviceQueue(logicalDevice, physicalDeviceInformation.getQueueFamilyIndexGraphics(), 0, pQueue);
            graphicsQueue = new VkQueue(pQueue.get(0), logicalDevice);

            vkGetDeviceQueue(logicalDevice, physicalDeviceInformation.getQueueFamilyIndexSurface(), 0, pQueue);
            presentQueue = new VkQueue(pQueue.get(0), logicalDevice);
            LOGGER.debug("Initialized logical device.");
        }
    }

    private void initMemoryManager() {
        LOGGER.debug("Initializing memory manager...");
        memoryManager = new MemoryManager(this);
        memoryManager.init();
        LOGGER.debug("Initialized memory manager.");
    }

    private void initSceneCamera() {
        sceneCamera = new SceneCamera();
        float fieldOfViewY = (float) (Math.PI / 4);
        float aspect = ((float) config.getWidth() / (float) config.getHeight());
        sceneCamera.setProjection(new PerspectiveProjection(fieldOfViewY, aspect));
        // TODO: Offer possibility to automatically update projection when window resizes
    }

    private void initGuiCamera() {
        guiCamera = new GuiCamera();
        addWindowResizeListener(guiCamera);
        guiCamera.onWindowResize(config.getWidth(), config.getHeight());
    }

    private void mainLoop() {
        LOGGER.debug("Started main loop.");
        while (!(glfwWindowShouldClose(window) || stop)) {
            glfwPollEvents();
            float tpf = calculateNextTpf();
            if (!isInitialized) {
                init();
                isInitialized = true;
            }
            inputManager.processEvents();
            systems.forEach(system -> system.update(tpf));
            update(tpf);
            updateInternalState(tpf);
            swapChain.drawNextFrame();
        }
        LOGGER.debug("Finished main loop.");
        vkDeviceWaitIdle(logicalDevice);
    }

    private float calculateNextTpf() {
        float currentTime = (float) glfwGetTime();
        float tpf = (currentTime - time);
        time = currentTime;
        return tpf;
    }

    protected void init() {

    }

    protected void update(float tpf) {

    }

    public void updateInternalState() {
        updateInternalState(0);
    }

    private void updateInternalState(float tpf) {
        updateLogicalState(tpf);
        applyLogicalState();
        updateNativeState();
    }

    private void updateLogicalState(float tpf) {
        if (light != null) {
            light.updateLogicalState(this, tpf);
        }
        rootNode.updateLogicalState(this, tpf);
    }

    private void applyLogicalState() {
        if (light != null) {
            light.applyLogicalState();
        }
        rootNode.applyLogicalState();
        bucketRenderer.updateSplitOrderedGeometries();
    }

    private void updateNativeState() {
        shaderManager.updateNative(this);
        pipelineManager.updateNative(this);
        commandPool.updateNative(this);
        sceneCamera.updateNative(this);
        guiCamera.updateNative(this);
        if (light != null) {
            light.updateNativeState(this);
        }
        rootNode.updateNativeState(this);
        swapChain.updateNative(this);
    }

    public void setLight(Light light) {
        light.setModified(true);
        this.light = light;
    }

    public void addFilter(Filter filter) {
        filters.add(filter);
        swapChain.getRenderJobManager().addPostSceneRenderJob(filter.getFilterRenderJob());
    }

    public void removeFilter(Filter filter) {
        filters.remove(filter);
        swapChain.getRenderJobManager().removePostSceneRenderJob(filter.getFilterRenderJob());
    }

    public void addSystem(AppSystem system) {
        system.setApplication(this);
        systems.add(system);
        system.onAttached();
    }

    public boolean hasSystem(AppSystem system) {
        return systems.contains(system);
    }

    public void removeSystem(AppSystem system) {
        if (systems.remove(system)) {
            system.onDetached();
            system.setApplication(null);
        }
    }

    public void addWindowResizeListener(WindowResizeListener windowResizeListener) {
        windowResizeListeners.add(windowResizeListener);
    }

    public void removeWindowResizeListener(WindowResizeListener windowResizeListener) {
        windowResizeListeners.remove(windowResizeListener);
    }

    public Vector3f getWorldCoordinates(Vector2f screenPosition, float projectionZPos) {
        return getWorldCoordinates(screenPosition, projectionZPos, new Vector3f());
    }

    public Vector3f getWorldCoordinates(Vector2f screenPosition, float viewSpaceZ, Vector3f dest) {
        // TODO: Introduce TempVars
        Matrix4f viewProjectionMatrix = sceneCamera.getProjectionViewMatrix().invert(new Matrix4f());
        dest.set(
            ((screenPosition.x() / config.getWidth()) * 2) - 1,
            ((screenPosition.y() / config.getHeight()) * 2) - 1,
            viewSpaceZ
        );
        float w = MathUtil.mulW(dest, viewProjectionMatrix);
        MathUtil.mulPosition(dest, viewProjectionMatrix);
        dest.mul(1 / w);
        return dest;
    }

    public Vector3f getScreenCoordinates(Vector3f worldPosition) {
        return getScreenCoordinates(worldPosition, new Vector3f());
    }

    public Vector3f getScreenCoordinates(Vector3f worldPosition, Vector3f dest) {
        // TODO: Introduce TempVars
        Vector4f tmp = new Vector4f(worldPosition, 1);
        tmp.mul(sceneCamera.getProjectionViewMatrix());
        dest.x = (((tmp.x() / tmp.w()) + 1) / 2) * config.getWidth();
        dest.y = (((tmp.y() / tmp.w()) + 1) / 2) * config.getHeight();
        dest.z = (((tmp.z() / tmp.w()) + 1) / 2);
        return dest;
    }

    public void stop() {
        stop = true;
    }

    private void cleanupAndClose() {
        LOGGER.debug("Cleaning up application...");
        cleanup();
        inputManager.cleanup();
        assetManager.cleanup();
        cleanupNativeState();
        memoryManager.cleanup();
        vkDestroyDevice(logicalDevice, null);
        vkDestroySurfaceKHR(instance, surface, null);
        if (debugMessenger != null) {
            vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, null);
        }
        vkDestroyInstance(instance, null);
        glfwDestroyWindow(window);
        glfwTerminate();
        LOGGER.debug("Cleaned up application.");
    }

    protected void cleanup() {

    }

    public void cleanupNativeState() {
        assetManager.cleanupNative();
        swapChain.cleanupNative();
        pipelineManager.cleanupNative();
        shaderManager.cleanupNative();
        sceneCamera.cleanupNative();
        guiCamera.cleanupNative();
        rootNode.cleanupNativeState();
        if (light != null) {
            light.cleanupNativeState();
        }
        commandPool.cleanupNative();
    }
}
