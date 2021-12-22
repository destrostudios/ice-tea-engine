package com.destrostudios.icetea.samples.water;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2f;
import org.joml.Vector3f;

@Getter
@Setter
public class WaterConfig {

	// H0k, Hkt computations
	private int n = 256;
	private int l = 1000;
	private float amplitude = 2;
	private float windSpeed = 80;
	private float capillarSuppressFactor = 0.1f;
	// Normal computations
	private float normalStrength = 0.2f;
	// Tessellation
	private float tessellationFactor = 10;
	private float tessellationSlope = 2;
	private float tessellationShift = 0;
	private float uvScale = 40;
	// Geometry
	private int patches = 128;
	private float displacementRange = 100;
	private float displacementRangeSurrounding = 50;
	private float highDetailRangeGeometry = 50;
	private float displacementScale = 0.04f;
	private float choppiness = 0.02f;
	// Fragment
	private float highDetailRangeTexture = 10;
	private float capillarDownsampling = 9;
	private float capillarStrength = 2.5f;
	private String dudvMapFilePath = "textures/water/dudv.jpg";
	private float dudvDownsampling = 1;
	private float kReflection = 0.008f;
	private float kRefraction = 0.008f;
	private float reflectionBlendMinFactor = 0.25f;
	private float reflectionBlendMaxFactor = 0.75f;
	private float reflectionBlendMaxDistance = 1000;
	private Vector3f waterColor = new Vector3f(0.128f, 0.210f, 0.240f);
	private float eta = 0.15f;
	private float fresnelFactor = 4;
	// Movement
	private Vector2f windDirection = new Vector2f(0.70710677f, 0.70710677f);
	private float timeSpeed = 4;
	private float motionSpeed = 0.00001f;
	private float distortionSpeed = 0.1f;

}
