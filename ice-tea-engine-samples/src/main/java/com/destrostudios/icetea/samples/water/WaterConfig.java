package com.destrostudios.icetea.samples.water;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2f;
import org.joml.Vector4f;

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
	private float normalStrength = 0.32f;
	// Tessellation
	private float tessellationFactor = 10;
	private float tessellationSlope = 1.8f;
	private float tessellationShift = 0;
	private float uvScale = 48;
	// Geometry
	private int patches = 128;
	private float displacementRange = 100;
	private float displacementRangeSurrounding = 50;
	private float highDetailRangeGeometry = 50;
	private float displacementScale = 0.04f;
	private float choppiness = 0.04f;
	// Fragment
	private float highDetailRangeTexture = 10;
	private float capillarDownsampling = 8;
	private float capillarStrength = 2;
	private String dudvMapFilePath = "com/destrostudios/icetea/samples/textures/water/dudv.jpg";
	private float dudvDownsampling = 1;
	private float kReflection = 0.008f;
	private float kRefraction = 0.008f;
	private Vector4f waterColor = new Vector4f();
	private float eta = 0.15f;
	private float fresnelFactor = 5;
	// Movement
	private Vector2f windDirection = new Vector2f(0.70710677f, 0.70710677f);
	private float timeSpeed = 4;
	private float motionSpeed = 0.02f;
	private float distortionSpeed = 0.1f;

}
