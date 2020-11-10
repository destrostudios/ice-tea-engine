package com.destrostudios.icetea.core;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.VK10.*;

public class PhysicalDeviceManager {

    public PhysicalDeviceManager(Application application) {
        this.application = application;
    }
    private Application application;

    public PhysicalDeviceInformation pickPhysicalDevice() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer deviceCount = stack.ints(0);
            vkEnumeratePhysicalDevices(application.getInstance(), deviceCount, null);
            if (deviceCount.get(0) == 0) {
                throw new RuntimeException("Failed to find GPUs with Vulkan support");
            }
            PointerBuffer ppPhysicalDevices = stack.mallocPointer(deviceCount.get(0));
            vkEnumeratePhysicalDevices(application.getInstance(), deviceCount, ppPhysicalDevices);
            PhysicalDeviceInformation physicalDeviceInformation = null;
            for (int i = 0;i < ppPhysicalDevices.capacity();i++) {
                VkPhysicalDevice physicalDevice = new VkPhysicalDevice(ppPhysicalDevices.get(i), application.getInstance());
                physicalDeviceInformation = getPhysicalDeviceInformation(physicalDevice, application.getSurface());
                if (isDeviceSuitable(physicalDeviceInformation)) {
                    break;
                }
            }
            if (physicalDeviceInformation == null) {
                throw new RuntimeException("Failed to find a suitable GPU");
            }
            physicalDeviceInformation.setMaxSamples(getMaxSamples(physicalDeviceInformation.getPhysicalDevice()));
            return physicalDeviceInformation;
        }
    }

    public PhysicalDeviceInformation getPhysicalDeviceInformation(VkPhysicalDevice physicalDevice, long surface) {
        PhysicalDeviceInformation physicalDeviceInformation = new PhysicalDeviceInformation();
        physicalDeviceInformation.setPhysicalDevice(physicalDevice);
        try (MemoryStack stack = stackPush()) {
            // Queue families
            IntBuffer queueFamilyCount = stack.ints(0);
            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, queueFamilyCount, null);
            VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.mallocStack(queueFamilyCount.get(0), stack);
            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, queueFamilyCount, queueFamilies);
            IntBuffer surfaceSupport = stack.ints(VK_FALSE);
            for (int i = 0; i < queueFamilies.capacity(); i++) {
                if ((queueFamilies.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                    physicalDeviceInformation.setQueueFamilyIndexGraphics(i);
                }
                vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, surface, surfaceSupport);
                if (surfaceSupport.get(0) == VK_TRUE) {
                    physicalDeviceInformation.setQueueFamilyIndexSurface(i);
                }
            }

            // Device extensions
            IntBuffer extensionCount = stack.ints(0);
            vkEnumerateDeviceExtensionProperties(physicalDevice, (String) null, extensionCount, null);
            VkExtensionProperties.Buffer availableExtensions = VkExtensionProperties.mallocStack(extensionCount.get(0), stack);
            Set<VkExtensionProperties> deviceExtensions = availableExtensions.stream().collect(toSet());
            physicalDeviceInformation.setDeviceExtensions(deviceExtensions);

            // Surface
            IntBuffer tmpCount = stack.ints(0);

            vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, tmpCount, null);
            if (tmpCount.get(0) > 0) {
                VkSurfaceFormatKHR.Buffer surfaceFormats = VkSurfaceFormatKHR.mallocStack(tmpCount.get(0), stack);
                vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, tmpCount, surfaceFormats);
                physicalDeviceInformation.setSurfaceFormats(surfaceFormats);
            }

            vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, tmpCount, null);
            if (tmpCount.get(0) != 0) {
                IntBuffer surfacePresentModes = stack.mallocInt(tmpCount.get(0));
                vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, tmpCount, surfacePresentModes);
                physicalDeviceInformation.setSurfacePresentModes(surfacePresentModes);
            }

            // Anisotropy
            VkPhysicalDeviceFeatures supportedFeatures = VkPhysicalDeviceFeatures.mallocStack(stack);
            vkGetPhysicalDeviceFeatures(physicalDevice, supportedFeatures);
            physicalDeviceInformation.setAnisotropySupported(supportedFeatures.samplerAnisotropy());
        }
        return physicalDeviceInformation;
    }

    private boolean isDeviceSuitable(PhysicalDeviceInformation physicalDeviceInformation) {
        return ((physicalDeviceInformation.getQueueFamilyIndexGraphics() != -1)
             && (physicalDeviceInformation.getQueueFamilyIndexSurface() != -1)
             && physicalDeviceInformation.getDeviceExtensions().containsAll(Application.DEVICE_EXTENSIONS_NAMES)
             && physicalDeviceInformation.getSurfaceFormats().hasRemaining()
             && physicalDeviceInformation.getSurfacePresentModes().hasRemaining()
             && physicalDeviceInformation.isAnisotropySupported());
    }

    private int getMaxSamples(VkPhysicalDevice physicalDevice) {
        try (MemoryStack stack = stackPush()) {
            VkPhysicalDeviceProperties physicalDeviceProperties = VkPhysicalDeviceProperties.mallocStack(stack);
            vkGetPhysicalDeviceProperties(physicalDevice, physicalDeviceProperties);

            int sampleCountFlags = physicalDeviceProperties.limits().framebufferColorSampleCounts()
                                 & physicalDeviceProperties.limits().framebufferDepthSampleCounts();

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

    public VkSurfaceCapabilitiesKHR getSurfaceCapabilities(MemoryStack stack) {
        VkSurfaceCapabilitiesKHR surfaceCapabilities = VkSurfaceCapabilitiesKHR.mallocStack(stack);
        vkGetPhysicalDeviceSurfaceCapabilitiesKHR(application.getPhysicalDevice(), application.getSurface(), surfaceCapabilities);
        return surfaceCapabilities;
    }
}
