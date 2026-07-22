package com.destrostudios.icetea.core.device;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.vulkan.VkPhysicalDevice;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Getter
@Setter
public class PhysicalDeviceInformation {

    private VkPhysicalDevice physicalDevice;
    private int queueFamilyIndexGraphics = -1;
    private int queueFamilyIndexSurface = -1;
    private Set<String> deviceExtensionNames;
    private List<SurfaceFormat> surfaceFormats;
    private int[] surfacePresentModes;
    private boolean hasSamplerAnisotropyFeature;
    private int maxSamples;
    private int depthStencilResolveMode;

    public int[] getUniqueQueueFamilyIndices() {
        return IntStream.of(queueFamilyIndexGraphics, queueFamilyIndexSurface).distinct().toArray();
    }
}
