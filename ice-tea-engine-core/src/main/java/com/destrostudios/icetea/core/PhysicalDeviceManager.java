package com.destrostudios.icetea.core;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRDepthStencilResolve.*;
import static org.lwjgl.vulkan.KHRGetPhysicalDeviceProperties2.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_PROPERTIES_2_KHR;
import static org.lwjgl.vulkan.KHRGetPhysicalDeviceProperties2.vkGetPhysicalDeviceProperties2KHR;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.VK10.*;

public class PhysicalDeviceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PhysicalDeviceManager.class);

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
            PhysicalDeviceInformation physicalDeviceInformation = null;
            for (int i = 0; i < ppPhysicalDevices.capacity(); i++) {
                VkPhysicalDevice physicalDevice = new VkPhysicalDevice(ppPhysicalDevices.get(i), application.getInstance());
                physicalDeviceInformation = getPhysicalDeviceInformation(physicalDevice, application.getSurface());
                boolean deviceSuitable = isDeviceSuitable(physicalDeviceInformation);
                LOGGER.debug("Physical device #{} suitable: {}", i, deviceSuitable);
                if (deviceSuitable) {
                    break;
                }
            }
            if (physicalDeviceInformation == null) {
                throw new RuntimeException("Failed to find a suitable GPU");
            }
            physicalDeviceInformation.setMaxSamples(getMaxSamples(physicalDeviceInformation.getPhysicalDevice()));
            physicalDeviceInformation.setDepthStencilResolveMode(getDepthStencilResolveMode(physicalDeviceInformation.getPhysicalDevice()));
            return physicalDeviceInformation;
        }
    }

    public PhysicalDeviceInformation getPhysicalDeviceInformation(VkPhysicalDevice physicalDevice, long surface) {
        LOGGER.debug("Fetching physical device information...");
        PhysicalDeviceInformation physicalDeviceInformation = new PhysicalDeviceInformation();
        physicalDeviceInformation.setPhysicalDevice(physicalDevice);
        try (MemoryStack stack = stackPush()) {
            // Queue families
            IntBuffer queueFamilyCount = stack.ints(0);
            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, queueFamilyCount, null);
            LOGGER.debug("Found {} physical device queue family properties.", queueFamilyCount.get(0));
            VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.mallocStack(queueFamilyCount.get(0), stack);
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
            VkExtensionProperties.Buffer availableExtensions = VkExtensionProperties.mallocStack(extensionCount.get(0), stack);
            Set<VkExtensionProperties> deviceExtensions = availableExtensions.stream().collect(toSet());
            physicalDeviceInformation.setDeviceExtensions(deviceExtensions);

            // Surface
            IntBuffer tmpCount = stack.ints(0);

            LOGGER.debug("Fetching physical device surface formats...");
            vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, tmpCount, null);
            LOGGER.debug("Found {} physical device surface formats.", tmpCount.get(0));
            if (tmpCount.get(0) > 0) {
                VkSurfaceFormatKHR.Buffer surfaceFormats = VkSurfaceFormatKHR.mallocStack(tmpCount.get(0), stack);
                vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, tmpCount, surfaceFormats);
                LOGGER.debug("Fetched {} physical device surface formats.", tmpCount.get(0));
                physicalDeviceInformation.setSurfaceFormats(surfaceFormats);
            }

            LOGGER.debug("Fetching physical device surface present modes...");
            vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, tmpCount, null);
            LOGGER.debug("Found {} physical device surface present modes.", tmpCount.get(0));
            if (tmpCount.get(0) != 0) {
                IntBuffer surfacePresentModes = stack.mallocInt(tmpCount.get(0));
                vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, tmpCount, surfacePresentModes);
                LOGGER.debug("Fetched {} physical device surface present modes.", tmpCount.get(0));
                physicalDeviceInformation.setSurfacePresentModes(surfacePresentModes);
            }

            // Anisotropy
            VkPhysicalDeviceFeatures supportedFeatures = VkPhysicalDeviceFeatures.mallocStack(stack);
            LOGGER.debug("Fetching physical device features...");
            vkGetPhysicalDeviceFeatures(physicalDevice, supportedFeatures);
            // TODO: Log all features
            LOGGER.debug("Fetched physical device features.");
            physicalDeviceInformation.setFeatures(supportedFeatures);
        }
        LOGGER.debug("Fetched physical device information.");
        return physicalDeviceInformation;
    }

    private boolean isDeviceSuitable(PhysicalDeviceInformation physicalDeviceInformation) {
        return ((physicalDeviceInformation.getQueueFamilyIndexGraphics() != -1)
             && (physicalDeviceInformation.getQueueFamilyIndexSurface() != -1)
             // TODO: This check won't work right now, it has to check the .extensionName() property, not the extension objcet
             && physicalDeviceInformation.getDeviceExtensions().containsAll(Application.DEVICE_EXTENSIONS_NAMES)
             && physicalDeviceInformation.getSurfaceFormats().hasRemaining()
             && physicalDeviceInformation.getSurfacePresentModes().hasRemaining()
             && physicalDeviceInformation.getFeatures().samplerAnisotropy());
    }

    private int getMaxSamples(VkPhysicalDevice physicalDevice) {
        try (MemoryStack stack = stackPush()) {
            LOGGER.debug("Fetching physical device properties...");
            VkPhysicalDeviceProperties physicalDeviceProperties = VkPhysicalDeviceProperties.mallocStack(stack);
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
            VkPhysicalDeviceProperties2KHR physicalDeviceProperties2 = VkPhysicalDeviceProperties2KHR.callocStack(stack);
            physicalDeviceProperties2.sType(VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_PROPERTIES_2_KHR);

            VkPhysicalDeviceDepthStencilResolvePropertiesKHR depthStencilResolveProperties = VkPhysicalDeviceDepthStencilResolvePropertiesKHR.callocStack(stack);
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
