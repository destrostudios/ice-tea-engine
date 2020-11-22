package com.destrostudios.icetea.core;

import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import static org.lwjgl.vulkan.VK10.*;

public class VertexDescriptions {

    private static final int OFFSET_POSITION = 0;
    private static final int OFFSET_COLOR = OFFSET_POSITION + (3 * Float.BYTES);
    private static final int OFFSET_TEX_COORDS = OFFSET_COLOR + (3 * Float.BYTES);
    private static final int OFFSET_NORMAL = OFFSET_TEX_COORDS + (2 * Float.BYTES);
    public static final int SIZEOF = OFFSET_NORMAL + (3 * Float.BYTES);

    public static VkVertexInputBindingDescription.Buffer getBindingDescription() {
        VkVertexInputBindingDescription.Buffer bindingDescription =  VkVertexInputBindingDescription.callocStack(1);
        bindingDescription.binding(0);
        bindingDescription.stride(SIZEOF);
        bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);
        return bindingDescription;
    }

    public static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions_All() {
        VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.callocStack(4);
        setPosition(attributeDescriptions, 0);
        setColor(attributeDescriptions, 1);
        setTextureCoordinates(attributeDescriptions, 2);
        setNormal(attributeDescriptions, 3);
        return attributeDescriptions.rewind();
    }

    public static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions_PositionOnly() {
        VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.callocStack(1);
        setPosition(attributeDescriptions, 0);
        return attributeDescriptions.rewind();
    }

    private static void setPosition(VkVertexInputAttributeDescription.Buffer attributeDescriptions, int index) {
        VkVertexInputAttributeDescription attributeDescription = attributeDescriptions.get(index);
        attributeDescription.binding(0);
        attributeDescription.location(index);
        attributeDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
        attributeDescription.offset(OFFSET_POSITION);
    }

    private static void setColor(VkVertexInputAttributeDescription.Buffer attributeDescriptions, int index) {
        VkVertexInputAttributeDescription attributeDescription = attributeDescriptions.get(index);
        attributeDescription.binding(0);
        attributeDescription.location(index);
        attributeDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
        attributeDescription.offset(OFFSET_COLOR);
    }

    private static void setTextureCoordinates(VkVertexInputAttributeDescription.Buffer attributeDescriptions, int index) {
        VkVertexInputAttributeDescription attributeDescription = attributeDescriptions.get(index);
        attributeDescription.binding(0);
        attributeDescription.location(index);
        attributeDescription.format(VK_FORMAT_R32G32_SFLOAT);
        attributeDescription.offset(OFFSET_TEX_COORDS);
    }

    private static void setNormal(VkVertexInputAttributeDescription.Buffer attributeDescriptions, int index) {
        VkVertexInputAttributeDescription attributeDescription = attributeDescriptions.get(index);
        attributeDescription.binding(0);
        attributeDescription.location(index);
        attributeDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
        attributeDescription.offset(OFFSET_NORMAL);
    }
}
