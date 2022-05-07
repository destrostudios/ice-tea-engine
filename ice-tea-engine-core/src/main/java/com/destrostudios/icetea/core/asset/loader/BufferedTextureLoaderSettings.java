package com.destrostudios.icetea.core.asset.loader;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static org.lwjgl.vulkan.VK10.*;

@Builder
@Setter
@Getter
public class BufferedTextureLoaderSettings {
    @Builder.Default
    private int format = VK_FORMAT_R8G8B8A8_SRGB;
    @Builder.Default
    private int usage = VK_IMAGE_USAGE_SAMPLED_BIT;
    @Builder.Default
    private int layout = VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
    @Builder.Default
    private boolean createDefaultDescriptor = true;
}
