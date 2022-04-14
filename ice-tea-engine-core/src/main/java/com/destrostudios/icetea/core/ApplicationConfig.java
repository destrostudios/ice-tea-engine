package com.destrostudios.icetea.core;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector4f;

import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR;

@Getter
@Setter
public class ApplicationConfig {

	private boolean enableValidationLayer;
	private boolean displayFpsInTitle;
	private String title = "IceTea Engine";
	private int width = 1280;
	private int height = 720;
	private int preferredPresentMode = VK_PRESENT_MODE_MAILBOX_KHR;
	private Vector4f clearColor = new Vector4f(0, 0, 0, 1);
	private int framesInFlight = 2;

}
