package com.destrostudios.icetea.core.render.shadow;

import lombok.Getter;
import lombok.Setter;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_D16_UNORM;

@Getter
@Setter
public class ShadowConfig {

	private int shadowMapFormat = VK_FORMAT_D16_UNORM;
	private int shadowMapSize = 4096;
	private float brightness = 0.1f;
	private int cascadesCount = 4;
	private float[] cascadeSplits;
	private float cascadeSplitLambda = 0.8f;
	private boolean cascadeDebugColors = true;

}
