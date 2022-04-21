package com.destrostudios.icetea.core;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import java.nio.IntBuffer;
import java.util.Set;
import java.util.stream.IntStream;

@Getter
@Setter
public class PhysicalDeviceInformation {

    private VkPhysicalDevice physicalDevice;
    private int queueFamilyIndexGraphics = -1;
    private int queueFamilyIndexSurface = -1;
    private Set<VkExtensionProperties> deviceExtensions;
    private VkSurfaceFormatKHR.Buffer surfaceFormats;
    private IntBuffer surfacePresentModes;
    private VkPhysicalDeviceFeatures features;
    private int maxSamples;
    private int depthStencilResolveMode;

    public int[] getUniqueQueueFamilyIndices() {
        return IntStream.of(queueFamilyIndexGraphics, queueFamilyIndexSurface).distinct().toArray();
    }
}
