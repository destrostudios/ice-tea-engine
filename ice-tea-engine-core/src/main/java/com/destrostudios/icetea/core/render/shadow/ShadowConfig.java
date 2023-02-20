package com.destrostudios.icetea.core.render.shadow;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShadowConfig {

	private int shadowMapSize = 4096;
	private float brightness = 0.1f;
	private int cascadesCount = 4;
	private float[] cascadeSplits;
	private float cascadeSplitLambda = 0.8f;
	private boolean cascadeDebugColors = true;

}
