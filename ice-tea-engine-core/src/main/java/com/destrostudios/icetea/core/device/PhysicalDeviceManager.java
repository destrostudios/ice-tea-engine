package com.destrostudios.icetea.core.device;

import com.destrostudios.icetea.core.Application;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRCreateRenderpass2.VK_KHR_CREATE_RENDERPASS_2_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRDepthStencilResolve.*;
import static org.lwjgl.vulkan.KHRGetPhysicalDeviceProperties2.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_PROPERTIES_2_KHR;
import static org.lwjgl.vulkan.KHRGetPhysicalDeviceProperties2.vkGetPhysicalDeviceProperties2KHR;
import static org.lwjgl.vulkan.KHRMaintenance2.VK_KHR_MAINTENANCE2_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRMultiview.VK_KHR_MULTIVIEW_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;

public class PhysicalDeviceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PhysicalDeviceManager.class);

    private static final Set<String> REQUIRED_DEVICE_EXTENSIONS_NAMES = Stream.of(
        // Required to use swapchains
        VK_KHR_SWAPCHAIN_EXTENSION_NAME,
        // Required to resolve multisampled depth buffers (for postprocessing)
        VK_KHR_MULTIVIEW_EXTENSION_NAME,
        VK_KHR_MAINTENANCE2_EXTENSION_NAME,
        VK_KHR_CREATE_RENDERPASS_2_EXTENSION_NAME,
        VK_KHR_DEPTH_STENCIL_RESOLVE_EXTENSION_NAME
    ).collect(toSet());

    public PhysicalDeviceManager(Application application) {
        this.application = application;
    }
    private Application application;

    public PhysicalDeviceInformation pickPhysicalDevice() {
        try (MemoryStack stack = stackPush()) {
            LOGGER.debug("Fetching physical devices...");
            IntBuffer deviceCount = stack.ints(0);
            vkEnumeratePhysicalDevices(application.getInstance(), deviceCount, null);
            if (deviceCount.get(0) == 0) {
                throw new RuntimeException("Failed to find GPUs with Vulkan support");
            }
            LOGGER.debug("Found {} physical devices.", deviceCount.get(0));
            PointerBuffer ppPhysicalDevices = stack.mallocPointer(deviceCount.get(0));
            vkEnumeratePhysicalDevices(application.getInstance(), deviceCount, ppPhysicalDevices);
            LOGGER.debug("Fetched {} physical devices.", deviceCount.get(0));
            PhysicalDeviceInformation suitableDeviceInformation = null;
            for (int i = 0; i < ppPhysicalDevices.capacity(); i++) {
                VkPhysicalDevice physicalDevice = new VkPhysicalDevice(ppPhysicalDevices.get(i), application.getInstance());
                PhysicalDeviceInformation deviceInformation = getPhysicalDeviceInformation(physicalDevice, application.getSurface());
                boolean deviceSuitable = isDeviceSuitable(deviceInformation);
                LOGGER.debug("Physical device #{} suitable: {}", i, deviceSuitable);
                if (deviceSuitable) {
                    suitableDeviceInformation = deviceInformation;
                    break;
                }
            }
            if (suitableDeviceInformation == null) {
                throw new RuntimeException("Failed to find a suitable GPU");
            }
            suitableDeviceInformation.setMaxSamples(getMaxSamples(suitableDeviceInformation.getPhysicalDevice()));
            suitableDeviceInformation.setDepthStencilResolveMode(getDepthStencilResolveMode(suitableDeviceInformation.getPhysicalDevice()));
            return suitableDeviceInformation;
        }
    }

    private PhysicalDeviceInformation getPhysicalDeviceInformation(VkPhysicalDevice physicalDevice, long surface) {
        LOGGER.debug("Fetching physical device information...");
        PhysicalDeviceInformation physicalDeviceInformation = new PhysicalDeviceInformation();
        physicalDeviceInformation.setPhysicalDevice(physicalDevice);
        try (MemoryStack stack = stackPush()) {
            // Queue families
            IntBuffer queueFamilyCount = stack.ints(0);
            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, queueFamilyCount, null);
            LOGGER.debug("Found {} physical device queue family properties.", queueFamilyCount.get(0));
            VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.malloc(queueFamilyCount.get(0), stack);
            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, queueFamilyCount, queueFamilies);
            LOGGER.debug("Fetched {} physical device queue family properties.", queueFamilyCount.get(0));
            IntBuffer surfaceSupport = stack.ints(VK_FALSE);
            for (int i = 0; i < queueFamilies.capacity(); i++) {
                boolean hasGraphicsSupport = ((queueFamilies.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0);
                LOGGER.debug("Queue family #{} graphics support: {}", i, hasGraphicsSupport);
                if (hasGraphicsSupport) {
                    physicalDeviceInformation.setQueueFamilyIndexGraphics(i);
                }
                LOGGER.debug("Fetching queue #{} family surface support...", i);
                vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, surface, surfaceSupport);
                boolean hasSurfaceSupport = (surfaceSupport.get(0) == VK_TRUE);
                LOGGER.debug("Fetched queue #{} family surface support: {}", i, hasSurfaceSupport);
                if (hasSurfaceSupport) {
                    physicalDeviceInformation.setQueueFamilyIndexSurface(i);
                }
            }

            // Device extensions
            IntBuffer extensionCount = stack.ints(0);
            LOGGER.debug("Fetching physical device extension properties...");
            vkEnumerateDeviceExtensionProperties(physicalDevice, (String) null, extensionCount, null);
            LOGGER.debug("Found {} physical device extension properties...", extensionCount.get(0));
            // Allocate outside stack (and free manually), as the extension list can exceed the default MemoryStack capacity
            VkExtensionProperties.Buffer availableExtensions = VkExtensionProperties.malloc(extensionCount.get(0));
            try {
                vkEnumerateDeviceExtensionProperties(physicalDevice, (String) null, extensionCount, availableExtensions);
                Set<String> deviceExtensions = availableExtensions.stream().map(VkExtensionProperties::extensionNameString).collect(toSet());
                physicalDeviceInformation.setDeviceExtensionNames(deviceExtensions);
            } finally {
                availableExtensions.free();
            }

            // Surface
            IntBuffer tmpCount = stack.ints(0);

            LOGGER.debug("Fetching physical device surface formats...");
            vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, tmpCount, null);
            LOGGER.debug("Found {} physical device surface formats.", tmpCount.get(0));
            if (tmpCount.get(0) > 0) {
                VkSurfaceFormatKHR.Buffer surfaceFormatsBuffer = VkSurfaceFormatKHR.malloc(tmpCount.get(0), stack);
                vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, tmpCount, surfaceFormatsBuffer);
                LOGGER.debug("Fetched {} physical device surface formats.", tmpCount.get(0));
                List<SurfaceFormat> surfaceFormats = surfaceFormatsBuffer.stream().map(format -> new SurfaceFormat(format.format(), format.colorSpace())).toList();
                physicalDeviceInformation.setSurfaceFormats(surfaceFormats);
            }

            LOGGER.debug("Fetching physical device surface present modes...");
            vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, tmpCount, null);
            LOGGER.debug("Found {} physical device surface present modes.", tmpCount.get(0));
            if (tmpCount.get(0) != 0) {
                IntBuffer surfacePresentModesBuffer = stack.mallocInt(tmpCount.get(0));
                vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, tmpCount, surfacePresentModesBuffer);
                LOGGER.debug("Fetched {} physical device surface present modes.", tmpCount.get(0));
                int[] surfacePresentModes = new int[surfacePresentModesBuffer.remaining()];
                surfacePresentModesBuffer.get(surfacePresentModes);
                physicalDeviceInformation.setSurfacePresentModes(surfacePresentModes);
            }

            // Anisotropy
            VkPhysicalDeviceFeatures supportedFeatures = VkPhysicalDeviceFeatures.malloc(stack);
            LOGGER.debug("Fetching physical device features...");
            vkGetPhysicalDeviceFeatures(physicalDevice, supportedFeatures);
            // TODO: Log all features
            LOGGER.debug("Fetched physical device features.");
            physicalDeviceInformation.setHasSamplerAnisotropyFeature(supportedFeatures.samplerAnisotropy());
        }
        LOGGER.debug("Fetched physical device information.");
        return physicalDeviceInformation;
    }

    private boolean isDeviceSuitable(PhysicalDeviceInformation physicalDeviceInformation) {
        return ((physicalDeviceInformation.getQueueFamilyIndexGraphics() != -1)
             && (physicalDeviceInformation.getQueueFamilyIndexSurface() != -1)
             && physicalDeviceInformation.getDeviceExtensionNames().containsAll(REQUIRED_DEVICE_EXTENSIONS_NAMES)
             && physicalDeviceInformation.getSurfaceFormats().size() > 0
             && physicalDeviceInformation.getSurfacePresentModes().length > 0
             && physicalDeviceInformation.isHasSamplerAnisotropyFeature());
    }

    private int getMaxSamples(VkPhysicalDevice physicalDevice) {
        try (MemoryStack stack = stackPush()) {
            LOGGER.debug("Fetching physical device properties...");
            VkPhysicalDeviceProperties physicalDeviceProperties = VkPhysicalDeviceProperties.malloc(stack);
            vkGetPhysicalDeviceProperties(physicalDevice, physicalDeviceProperties);
            LOGGER.debug("Fetched physical device properties.");

            int sampleCountFlags = physicalDeviceProperties.limits().framebufferColorSampleCounts()
                                 & physicalDeviceProperties.limits().framebufferDepthSampleCounts();
            LOGGER.debug("SampleCount flags: {}", sampleCountFlags);

            if ((sampleCountFlags & VK_SAMPLE_COUNT_64_BIT) != 0) {
                return VK_SAMPLE_COUNT_64_BIT;
            } else if ((sampleCountFlags & VK_SAMPLE_COUNT_32_BIT) != 0) {
                return VK_SAMPLE_COUNT_32_BIT;
            } else if ((sampleCountFlags & VK_SAMPLE_COUNT_16_BIT) != 0) {
                return VK_SAMPLE_COUNT_16_BIT;
            } else if ((sampleCountFlags & VK_SAMPLE_COUNT_8_BIT) != 0) {
                return VK_SAMPLE_COUNT_8_BIT;
            } else if ((sampleCountFlags & VK_SAMPLE_COUNT_4_BIT) != 0) {
                return VK_SAMPLE_COUNT_4_BIT;
            } else if ((sampleCountFlags & VK_SAMPLE_COUNT_2_BIT) != 0) {
                return VK_SAMPLE_COUNT_2_BIT;
            }
            return VK_SAMPLE_COUNT_1_BIT;
        }
    }

    private int getDepthStencilResolveMode(VkPhysicalDevice physicalDevice) {
        try (MemoryStack stack = stackPush()) {
            LOGGER.debug("Fetching physical device properties (2KHR)...");
            VkPhysicalDeviceProperties2KHR physicalDeviceProperties2 = VkPhysicalDeviceProperties2KHR.calloc(stack);
            physicalDeviceProperties2.sType(VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_PROPERTIES_2_KHR);

            VkPhysicalDeviceDepthStencilResolvePropertiesKHR depthStencilResolveProperties = VkPhysicalDeviceDepthStencilResolvePropertiesKHR.calloc(stack);
            depthStencilResolveProperties.sType(VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_DEPTH_STENCIL_RESOLVE_PROPERTIES_KHR);
            physicalDeviceProperties2.pNext(depthStencilResolveProperties.address());

            vkGetPhysicalDeviceProperties2KHR(physicalDevice, physicalDeviceProperties2);
            LOGGER.debug("Fetched physical device properties (2KHR).");

            // We currently use the same one for simplicity, could easily be separated
            int resolveModeFlags = depthStencilResolveProperties.supportedDepthResolveModes()
                                 & depthStencilResolveProperties.supportedStencilResolveModes();
            LOGGER.debug("ResolveMode flags: {}", resolveModeFlags);

            if ((resolveModeFlags & VK_RESOLVE_MODE_SAMPLE_ZERO_BIT_KHR) != 0) {
                return VK_RESOLVE_MODE_SAMPLE_ZERO_BIT_KHR;
            } else if ((resolveModeFlags & VK_RESOLVE_MODE_MAX_BIT_KHR) != 0) {
                return VK_RESOLVE_MODE_MAX_BIT_KHR;
            }
            return VK_RESOLVE_MODE_MIN_BIT_KHR;
        }
    }
}
