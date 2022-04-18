package com.destrostudios.icetea.samples.terrain;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2f;
import org.joml.Vector4f;

@Getter
@Setter
public class GrassConfig {

	// Mesh
	private int patches = 10;
	// Tessellation
	private float tessellationFactor = 20;
	private float tessellationSlope = 1;
	private float tessellationShift = 0;
	// Geometry
	private float bladeWidth = 0.02f;
	private float bladeHeight = 0.3f;
	private float bladeForward = 0.1f;
	private float bladeBend = 2;
	private int bladeSegments = 4;
	private Vector2f windVelocity = new Vector2f(0.03f, 0.015f);
	private float windFrequency = 1;
	private String windMapFilePath = "com/destrostudios/icetea/samples/textures/grass/wind.png";
	private float windMaxAngle = (float) (0.25 * Math.PI);
	// Fragment
	private Vector4f baseColor = new Vector4f(0.02f, 0.15f, 0.02f, 1);
	private Vector4f tipColor = new Vector4f(0.09f, 0.4f, 0.09f, 1);
	private String bladeMapFilePath = "com/destrostudios/icetea/samples/textures/grass/blade.png";

}
