package com.destrostudios.icetea.core.render.cubemap;

import lombok.Getter;
import lombok.Setter;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;

@Getter
@Setter
public class CubeMapConfig {

	private int size = 512;
	private int format = VK_FORMAT_R32G32B32A32_SFLOAT;
	private boolean generateMipMaps;

}
