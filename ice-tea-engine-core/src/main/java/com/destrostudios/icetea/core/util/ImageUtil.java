package com.destrostudios.icetea.core.util;

import static org.lwjgl.vulkan.VK10.*;

public class ImageUtil {

    public static int getRecommendedMipLevels(float size) {
        return (int) (Math.floor(MathUtil.log2(size)) + 1);
    }

    public static int getBytesPerPixel(int format) {
        return switch (format) {
            case VK_FORMAT_A1R5G5B5_UNORM_PACK16 -> 2;
            case VK_FORMAT_A2R10G10B10_SINT_PACK32 -> 4;
            case VK_FORMAT_A2R10G10B10_SNORM_PACK32 -> 4;
            case VK_FORMAT_A2R10G10B10_SSCALED_PACK32 -> 4;
            case VK_FORMAT_A2R10G10B10_UINT_PACK32 -> 4;
            case VK_FORMAT_A2R10G10B10_UNORM_PACK32 -> 4;
            case VK_FORMAT_A2R10G10B10_USCALED_PACK32 -> 4;
            case VK_FORMAT_A8B8G8R8_SINT_PACK32 -> 4;
            case VK_FORMAT_A8B8G8R8_SNORM_PACK32 -> 4;
            case VK_FORMAT_A8B8G8R8_SRGB_PACK32 -> 4;
            case VK_FORMAT_A8B8G8R8_SSCALED_PACK32 -> 4;
            case VK_FORMAT_A8B8G8R8_UINT_PACK32 -> 4;
            case VK_FORMAT_A8B8G8R8_UNORM_PACK32 -> 4;
            case VK_FORMAT_A8B8G8R8_USCALED_PACK32 -> 4;
            case VK_FORMAT_B4G4R4A4_UNORM_PACK16 -> 2;
            case VK_FORMAT_B5G5R5A1_UNORM_PACK16 -> 2;
            case VK_FORMAT_B5G6R5_UNORM_PACK16 -> 2;
            case VK_FORMAT_B8G8R8A8_SINT -> 4;
            case VK_FORMAT_B8G8R8A8_SNORM -> 4;
            case VK_FORMAT_B8G8R8A8_SRGB -> 4;
            case VK_FORMAT_B8G8R8A8_SSCALED -> 4;
            case VK_FORMAT_B8G8R8A8_UINT -> 4;
            case VK_FORMAT_B8G8R8A8_UNORM -> 4;
            case VK_FORMAT_B8G8R8A8_USCALED -> 4;
            case VK_FORMAT_B8G8R8_SINT -> 3;
            case VK_FORMAT_B8G8R8_SNORM -> 3;
            case VK_FORMAT_B8G8R8_SRGB -> 3;
            case VK_FORMAT_B8G8R8_SSCALED -> 3;
            case VK_FORMAT_B8G8R8_UINT -> 3;
            case VK_FORMAT_B8G8R8_UNORM -> 3;
            case VK_FORMAT_B8G8R8_USCALED -> 3;
            case VK_FORMAT_R16G16B16A16_SFLOAT -> 8;
            case VK_FORMAT_R16G16B16A16_SINT -> 8;
            case VK_FORMAT_R16G16B16A16_SNORM -> 8;
            case VK_FORMAT_R16G16B16A16_UINT -> 8;
            case VK_FORMAT_R16G16B16A16_UNORM -> 8;
            case VK_FORMAT_R16G16B16_SFLOAT -> 6;
            case VK_FORMAT_R16G16B16_SINT -> 6;
            case VK_FORMAT_R16G16B16_SNORM -> 6;
            case VK_FORMAT_R16G16B16_UINT -> 6;
            case VK_FORMAT_R16G16B16_UNORM -> 6;
            case VK_FORMAT_R16G16_SFLOAT -> 4;
            case VK_FORMAT_R16G16_SINT -> 4;
            case VK_FORMAT_R16G16_SNORM -> 4;
            case VK_FORMAT_R16G16_UINT -> 4;
            case VK_FORMAT_R16G16_UNORM -> 4;
            case VK_FORMAT_R16_SFLOAT -> 2;
            case VK_FORMAT_R16_SINT -> 2;
            case VK_FORMAT_R16_SNORM -> 2;
            case VK_FORMAT_R16_UINT -> 2;
            case VK_FORMAT_R16_UNORM -> 2;
            case VK_FORMAT_R32G32B32A32_SFLOAT -> 16;
            case VK_FORMAT_R32G32B32A32_SINT -> 16;
            case VK_FORMAT_R32G32B32A32_UINT -> 16;
            case VK_FORMAT_R32G32B32_SFLOAT -> 12;
            case VK_FORMAT_R32G32B32_SINT -> 12;
            case VK_FORMAT_R32G32B32_UINT -> 12;
            case VK_FORMAT_R32G32_SFLOAT -> 8;
            case VK_FORMAT_R32G32_SINT -> 8;
            case VK_FORMAT_R32G32_UINT -> 8;
            case VK_FORMAT_R32_SFLOAT -> 4;
            case VK_FORMAT_R32_SINT -> 4;
            case VK_FORMAT_R32_UINT -> 4;
            case VK_FORMAT_R4G4B4A4_UNORM_PACK16 -> 2;
            case VK_FORMAT_R4G4_UNORM_PACK8 -> 1;
            case VK_FORMAT_R5G5B5A1_UNORM_PACK16 -> 2;
            case VK_FORMAT_R5G6B5_UNORM_PACK16 -> 2;
            case VK_FORMAT_R8G8B8A8_SINT -> 4;
            case VK_FORMAT_R8G8B8A8_SNORM -> 4;
            case VK_FORMAT_R8G8B8A8_SRGB -> 4;
            case VK_FORMAT_R8G8B8A8_SSCALED -> 4;
            case VK_FORMAT_R8G8B8A8_UINT -> 4;
            case VK_FORMAT_R8G8B8A8_UNORM -> 4;
            case VK_FORMAT_R8G8B8A8_USCALED -> 4;
            case VK_FORMAT_R8G8B8_SINT -> 3;
            case VK_FORMAT_R8G8B8_SNORM -> 3;
            case VK_FORMAT_R8G8B8_SRGB -> 3;
            case VK_FORMAT_R8G8B8_SSCALED -> 3;
            case VK_FORMAT_R8G8B8_UINT -> 3;
            case VK_FORMAT_R8G8B8_UNORM -> 3;
            case VK_FORMAT_R8G8B8_USCALED -> 3;
            case VK_FORMAT_R8G8_SINT -> 2;
            case VK_FORMAT_R8G8_SNORM -> 2;
            case VK_FORMAT_R8G8_SRGB -> 2;
            case VK_FORMAT_R8G8_SSCALED -> 2;
            case VK_FORMAT_R8G8_UINT -> 2;
            case VK_FORMAT_R8G8_UNORM -> 2;
            case VK_FORMAT_R8G8_USCALED -> 2;
            case VK_FORMAT_R8_SINT -> 1;
            case VK_FORMAT_R8_SNORM -> 1;
            case VK_FORMAT_R8_SRGB -> 1;
            case VK_FORMAT_R8_SSCALED -> 1;
            case VK_FORMAT_R8_UINT -> 1;
            case VK_FORMAT_R8_UNORM -> 1;
            case VK_FORMAT_R8_USCALED -> 1;
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        };
    }
}
