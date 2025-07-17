package com.destrostudios.icetea.core.pbr;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PbrConfig {

	// Environment map
	private int environmentMapSize = 512;
	private boolean generateEnvironmentMapMipMaps = true;
	// Irradiance map
	private int irradianceMapSize = 64;
	private int irradianceMapSamplesHorizontal = 180;
	private int irradianceMapSamplesVertical = 64;
	// Prefiltered environment map
	private int prefilteredEnvironmentMapSize = 512;
	private int prefilteredEnvironmentMapSamples = 1024;
	// BRDF lookup texture
	private int brfdLookupTextureSize = 512;
	private int brfdLookupTextureSamples = 1024;
	// Other
	private boolean applyToneMapping;

}
