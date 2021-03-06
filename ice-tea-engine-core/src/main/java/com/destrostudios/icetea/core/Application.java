package com.destrostudios.icetea.core;

import com.destrostudios.icetea.core.asset.AssetManager;
import com.destrostudios.icetea.core.camera.Camera;
import com.destrostudios.icetea.core.camera.GuiCamera;
import com.destrostudios.icetea.core.camera.SceneCamera;
import com.destrostudios.icetea.core.filter.Filter;
import com.destrostudios.icetea.core.input.*;
import com.destrostudios.icetea.core.render.bucket.BucketRenderer;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.light.Light;
import com.destrostudios.icetea.core.scene.Node;
import com.destrostudios.icetea.core.shader.ShaderManager;
import com.destrostudios.icetea.core.util.BufferUtil;
import com.destrostudios.icetea.core.util.MathUtil;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.KHRCreateRenderpass2.VK_KHR_CREATE_RENDERPASS_2_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRDepthStencilResolve.VK_KHR_DEPTH_STENCIL_RESOLVE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRGetPhysicalDeviceProperties2.VK_KHR_GET_PHYSICAL_DEVICE_PROPERTIES_2_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRMaintenance2.VK_KHR_MAINTENANCE2_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRMultiview.VK_KHR_MULTIVIEW_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

public abstract class Application {

    static final Set<String> DEVICE_EXTENSIONS_NAMES = Stream.of(
        // Required to use swapchains
        VK_KHR_SWAPCHAIN_EXTENSION_NAME,
        // Required to resolve multisampled depth buffers (for postprocessing)
        VK_KHR_MULTIVIEW_EXTENSION_NAME,
        VK_KHR_MAINTENANCE2_EXTENSION_NAME,
        VK_KHR_CREATE_RENDERPASS_2_EXTENSION_NAME,
        VK_KHR_DEPTH_STENCIL_RESOLVE_EXTENSION_NAME
    ).collect(toSet());
    private static final int MAX_FRAMES_IN_FLIGHT = 2;

    @Getter
    private PhysicalDeviceManager physicalDeviceManager;
    @Getter
    private BufferManager bufferManager;
    @Getter
    private ImageManager imageManager;
    @Getter
    private ShaderManager shaderManager;
    @Getter
    protected InputManager inputManager;
    @Getter
    protected AssetManager assetManager;

    @Getter
    private VkInstance instance;
    @Getter
    private int width = 1280;
    @Getter
    private int height = 720;
    @Getter
    private long window;
    @Getter
    private long surface;
    private boolean wasResized;
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
    private VkQueue presentQueue;
    @Getter
    private long commandPool;
    @Getter
    private BucketRenderer bucketRenderer;
    @Getter
    private SwapChain swapChain;
    private boolean commandBuffersOutdated;
    protected float time;

    @Getter
    protected SceneCamera sceneCamera;
    @Getter
    private GuiCamera guiCamera;
    @Getter
    private Node rootNode;
    @Getter
    protected Node sceneNode;
    @Getter
    protected Node guiNode;
    @Getter
    private Light light;
    private List<Filter> filters;

    private List<Frame> inFlightFrames;
    private Map<Integer, Frame> imagesInFlight;
    private int currentFrame;

    public void start() {
        init();
        mainLoop();
        cleanup();
    }

    private void init() {
        physicalDeviceManager = new PhysicalDeviceManager(this);
        bufferManager = new BufferManager(this);
        imageManager = new ImageManager(this);
        shaderManager = new ShaderManager();
        inputManager = new InputManager(this);
        assetManager = new AssetManager();
        rootNode = new Node();
        sceneNode = new Node();
        rootNode.add(sceneNode);
        guiNode = new Node();
        rootNode.add(guiNode);
        filters = new LinkedList<>();
        initWindow();
        createInstance();
        initSurface();
        initPhysicalDevice();
        initLogicalDevice();
        initCommandPool();
        initSwapChain();
        initCameras();
        inputManager.init();
        initScene();
        initSyncObjects();
    }

    private void initWindow() {
        if (!glfwInit()) {
            throw new RuntimeException("Cannot initialize GLFW");
        }
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        window = glfwCreateWindow(width, height, "IceTea Engine", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Cannot create window");
        }
        glfwSetFramebufferSizeCallback(window, this::onFrameBufferResized);
    }

    private void onFrameBufferResized(long window, int width, int height) {
        this.width = width;
        this.height = height;
        wasResized = true;
        updateGuiCamera();
    }

    private void createInstance() {
        try (MemoryStack stack = stackPush()) {
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

            PointerBuffer instancePointer = stack.mallocPointer(1);
            if (vkCreateInstance(instanceCreateInfo, null, instancePointer) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create instance");
            }
            instance = new VkInstance(instancePointer.get(0), instanceCreateInfo);
        }
    }

    private PointerBuffer getRequiredExtensions(MemoryStack stack) {
        PointerBuffer glfwExtensions = glfwGetRequiredInstanceExtensions();
        PointerBuffer allRequiredExtensions = stack.mallocPointer(glfwExtensions.capacity() + 1);
        allRequiredExtensions.put(glfwExtensions);
        // Required to use the extension to resolve multisampled depth buffers (for postprocessing)
        allRequiredExtensions.put(stack.UTF8(VK_KHR_GET_PHYSICAL_DEVICE_PROPERTIES_2_EXTENSION_NAME));
        return allRequiredExtensions.rewind();
    }

    private void initSurface() {
        try (MemoryStack stack = stackPush()) {
            LongBuffer pSurface = stack.longs(VK_NULL_HANDLE);
            if (glfwCreateWindowSurface(instance, window, null, pSurface) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create window surface");
            }
            surface = pSurface.get(0);
        }
    }

    private void initPhysicalDevice() {
        physicalDeviceInformation = physicalDeviceManager.pickPhysicalDevice();
        physicalDevice = physicalDeviceInformation.getPhysicalDevice();
        msaaSamples = physicalDeviceInformation.getMaxSamples();
    }

    private void initLogicalDevice() {
        try (MemoryStack stack = stackPush()) {
            VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.callocStack(stack);
            deviceCreateInfo.sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO);
            deviceCreateInfo.ppEnabledExtensionNames(BufferUtil.asPointerBuffer(DEVICE_EXTENSIONS_NAMES));

            int[] uniqueQueueFamilyIndices = physicalDeviceInformation.getUniqueQueueFamilyIndices();
            VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.callocStack(uniqueQueueFamilyIndices.length, stack);
            for(int i = 0; i < uniqueQueueFamilyIndices.length; i++) {
                VkDeviceQueueCreateInfo queueCreateInfo = queueCreateInfos.get(i);
                queueCreateInfo.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
                queueCreateInfo.queueFamilyIndex(uniqueQueueFamilyIndices[i]);
                queueCreateInfo.pQueuePriorities(stack.floats(1.0f));
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
            if (vkCreateDevice(physicalDevice, deviceCreateInfo, null, pDevice) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create logical device");
            }
            logicalDevice = new VkDevice(pDevice.get(0), physicalDevice, deviceCreateInfo);

            PointerBuffer pQueue = stack.pointers(VK_NULL_HANDLE);

            vkGetDeviceQueue(logicalDevice, physicalDeviceInformation.getQueueFamilyIndexGraphics(), 0, pQueue);
            graphicsQueue = new VkQueue(pQueue.get(0), logicalDevice);

            vkGetDeviceQueue(logicalDevice, physicalDeviceInformation.getQueueFamilyIndexSurface(), 0, pQueue);
            presentQueue = new VkQueue(pQueue.get(0), logicalDevice);
        }
    }

    private void initCommandPool() {
        try (MemoryStack stack = stackPush()) {
            VkCommandPoolCreateInfo poolCreateInfo = VkCommandPoolCreateInfo.callocStack(stack);
            poolCreateInfo.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
            poolCreateInfo.queueFamilyIndex(physicalDeviceInformation.getQueueFamilyIndexGraphics());

            LongBuffer pCommandPool = stack.mallocLong(1);
            if (vkCreateCommandPool(logicalDevice, poolCreateInfo, null, pCommandPool) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create command pool");
            }
            commandPool = pCommandPool.get(0);
        }
    }

    private void initSwapChain() {
        sceneCamera = new SceneCamera();
        guiCamera = new GuiCamera();
        bucketRenderer = new BucketRenderer();
        swapChain = new SwapChain();
        swapChain.init(this);
        bucketRenderer.init(this);
    }

    private void initCameras() {
        sceneCamera.init(this);
        sceneCamera.setFieldOfViewY((float) Math.toRadians(45));
        sceneCamera.setAspect((float) swapChain.getExtent().width() / (float) swapChain.getExtent().height());
        sceneCamera.setZNear(0.1f);
        sceneCamera.setZFar(100);

        guiCamera.init(this);
        updateGuiCamera();
    }

    private void updateGuiCamera() {
        guiCamera.setApplicationSize(width, height);
    }

    protected abstract void initScene();

    private void initSyncObjects() {
        inFlightFrames = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
        imagesInFlight = new HashMap<>(swapChain.getImages().size());
        try (MemoryStack stack = stackPush()) {
            VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.callocStack(stack);
            semaphoreInfo.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

            VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.callocStack(stack);
            fenceInfo.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
            fenceInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT);

            LongBuffer pImageAvailableSemaphore = stack.mallocLong(1);
            LongBuffer pRenderFinishedSemaphore = stack.mallocLong(1);
            LongBuffer pFence = stack.mallocLong(1);

            for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
                if ((vkCreateSemaphore(logicalDevice, semaphoreInfo, null, pImageAvailableSemaphore) != VK_SUCCESS)
                 || (vkCreateSemaphore(logicalDevice, semaphoreInfo, null, pRenderFinishedSemaphore) != VK_SUCCESS)
                 || (vkCreateFence(logicalDevice, fenceInfo, null, pFence) != VK_SUCCESS)) {
                    throw new RuntimeException("Failed to create synchronization objects for the frame " + i);
                }
                inFlightFrames.add(new Frame(pImageAvailableSemaphore.get(0), pRenderFinishedSemaphore.get(0), pFence.get(0)));
            }
        }
    }

    public int findMemoryType(int typeFilter, int properties) {
        VkPhysicalDeviceMemoryProperties memoryProperties = VkPhysicalDeviceMemoryProperties.mallocStack();
        vkGetPhysicalDeviceMemoryProperties(physicalDevice, memoryProperties);
        for (int i = 0;i < memoryProperties.memoryTypeCount(); i++) {
            if (((typeFilter & (1 << i)) != 0) && ((memoryProperties.memoryTypes(i).propertyFlags() & properties) == properties)) {
                return i;
            }
        }
        throw new RuntimeException("Failed to find suitable memory type");
    }

    public void setLight(Light light) {
        light.setModified(true);
        this.light = light;
    }

    public void addFilter(Filter filter) {
        filters.add(filter);
        swapChain.getRenderJobManager().getQueuePostScene().add(filter.getFilterRenderJob());
        recreateRenderJobs();
    }

    public void removeFilter(Filter filter) {
        filter.cleanup();
        filters.remove(filter);
        swapChain.getRenderJobManager().getQueuePostScene().remove(filter.getFilterRenderJob());
        recreateRenderJobs();
    }

    public Vector3f getWorldCoordinates(Camera camera, Vector2f screenPosition, float projectionZPos) {
        return getWorldCoordinates(camera, screenPosition, projectionZPos, new Vector3f());
    }

    public Vector3f getWorldCoordinates(Camera camera, Vector2f screenPosition, float viewSpaceZ, Vector3f dest) {
        // TODO: Introduce TempVars
        Matrix4f viewProjectionMatrix = camera.getProjectionViewMatrix().invert(new Matrix4f());
        dest.set(
            ((screenPosition.x() / getWidth()) * 2) - 1,
            ((screenPosition.y() / getHeight()) * 2) - 1,
            viewSpaceZ
        );
        float w = MathUtil.mulW(dest, viewProjectionMatrix);
        MathUtil.mulPosition(dest, viewProjectionMatrix);
        dest.mul(1 / w);
        return dest;
    }

    private void mainLoop() {
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();
            updateState();
            drawFrame();
        }
        vkDeviceWaitIdle(logicalDevice);
    }

    private void updateState() {
        float tpf = calculateNextTpf();
        update(tpf);
        sceneCamera.update();
        updateLights();
        commandBuffersOutdated |= rootNode.update(this, tpf);
        if (commandBuffersOutdated) {
            swapChain.recreateCommandBuffers();
            commandBuffersOutdated = false;
        }
    }

    private float calculateNextTpf() {
        float currentTime = (float) glfwGetTime();
        float tpf = (currentTime - time);
        time = currentTime;
        return tpf;
    }

    protected abstract void update(float tpf);

    private void updateLights() {
        if (light != null) {
            light.update(this);
            if (light.isModified()) {
                swapChain.getRenderJobManager().getQueuePreScene().addAll(light.getShadowMapRenderJobs());
                recreateRenderJobs();
                light.setModified(false);
            }
        }
    }

    public void recreateRenderJobs() {
        swapChain.recreateRenderJobs();
        commandBuffersOutdated = true;
    }

    private void drawFrame() {
        try (MemoryStack stack = stackPush()) {
            Frame thisFrame = inFlightFrames.get(currentFrame);

            IntBuffer pImageIndex = stack.mallocInt(1);
            int vkResult = vkAcquireNextImageKHR(
                logicalDevice,
                swapChain.getSwapChain(),
                MathUtil.UINT64_MAX,
                thisFrame.getImageAvailableSemaphore(),
                VK_NULL_HANDLE,
                pImageIndex
            );
            if (vkResult == VK_ERROR_OUT_OF_DATE_KHR) {
                swapChain.recreate();
                return;
            } else if (vkResult != VK_SUCCESS) {
                throw new RuntimeException("Cannot get image");
            }
            int imageIndex = pImageIndex.get(0);
            updateUniformBuffers(imageIndex);

            if (imagesInFlight.containsKey(imageIndex)) {
                vkWaitForFences(logicalDevice, imagesInFlight.get(imageIndex).getFence(), true, MathUtil.UINT64_MAX);
            }
            imagesInFlight.put(imageIndex, thisFrame);

            VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack);
            submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.waitSemaphoreCount(1);
            submitInfo.pWaitSemaphores(thisFrame.getPImageAvailableSemaphore());
            submitInfo.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));
            submitInfo.pSignalSemaphores(thisFrame.getPRenderFinishedSemaphore());
            submitInfo.pCommandBuffers(stack.pointers(swapChain.getCommandBuffers().get(imageIndex)));
            vkResetFences(logicalDevice, thisFrame.getPFence());
            if ((vkResult = vkQueueSubmit(graphicsQueue, submitInfo, thisFrame.getFence())) != VK_SUCCESS) {
                vkResetFences(logicalDevice, thisFrame.getPFence());
                throw new RuntimeException("Failed to submit draw command buffer: " + vkResult);
            }

            VkPresentInfoKHR presentInfo = VkPresentInfoKHR.callocStack(stack);
            presentInfo.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
            presentInfo.pWaitSemaphores(thisFrame.getPRenderFinishedSemaphore());
            presentInfo.swapchainCount(1);
            presentInfo.pSwapchains(stack.longs(swapChain.getSwapChain()));
            presentInfo.pImageIndices(pImageIndex);
            vkResult = vkQueuePresentKHR(presentQueue, presentInfo);
            if ((vkResult == VK_ERROR_OUT_OF_DATE_KHR) || (vkResult == VK_SUBOPTIMAL_KHR) || wasResized) {
                wasResized = false;
                swapChain.recreate();
            } else if (vkResult != VK_SUCCESS) {
                throw new RuntimeException("Failed to present swap chain image");
            }

            currentFrame = ((currentFrame + 1) % MAX_FRAMES_IN_FLIGHT);

            // Wait for GPU to be finished, so we can safely access memory in our logic again (e.g. freeing memory of removed objects)
            vkWaitForFences(logicalDevice, thisFrame.getPFence(), true, MathUtil.UINT64_MAX);
        }
    }

    private void updateUniformBuffers(int currentImage) {
        swapChain.getRenderJobManager().forEachRenderJob(renderJob -> renderJob.updateUniformBuffers(currentImage));
        sceneCamera.getTransformUniformData().updateBufferIfNecessary(currentImage);
        guiCamera.getTransformUniformData().updateBufferIfNecessary(currentImage);
        if (light != null) {
            light.updateUniformBuffers(currentImage);
        }
        rootNode.updateUniformBuffers(currentImage);
    }

    private void cleanup() {
        inputManager.cleanup();

        shaderManager.cleanup();

        swapChain.cleanup();

        rootNode.forEachGeometry(Geometry::cleanup);

        sceneCamera.cleanup();
        guiCamera.cleanup();

        if (light != null) {
            light.cleanup();
        }

        filters.forEach(Filter::cleanup);

        inFlightFrames.forEach(frame -> {
            vkDestroySemaphore(logicalDevice, frame.getRenderFinishedSemaphore(), null);
            vkDestroySemaphore(logicalDevice, frame.getImageAvailableSemaphore(), null);
            vkDestroyFence(logicalDevice, frame.getFence(), null);
        });
        inFlightFrames.clear();

        vkDestroyCommandPool(logicalDevice, commandPool, null);

        vkDestroyDevice(logicalDevice, null);

        vkDestroySurfaceKHR(instance, surface, null);

        vkDestroyInstance(instance, null);

        glfwDestroyWindow(window);

        glfwTerminate();
    }
}
