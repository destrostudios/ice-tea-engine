package com.destrostudios.icetea.core.material;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import static org.lwjgl.vulkan.VK10.*;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class BlendMode {

	public static final BlendMode ALPHA = new BlendMode(
		VK_BLEND_FACTOR_SRC_ALPHA,
		VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA,
		VK_BLEND_OP_ADD,
		VK_BLEND_FACTOR_SRC_ALPHA,
		VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA,
		VK_BLEND_OP_ADD
	);
	public static final BlendMode PREMULTIPLIED_ALPHA = new BlendMode(
		VK_BLEND_FACTOR_ONE,
		VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA,
		VK_BLEND_OP_ADD,
		VK_BLEND_FACTOR_ONE,
		VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA,
		VK_BLEND_OP_ADD
	);
	public static final BlendMode ADDITIVE = new BlendMode(
		VK_BLEND_FACTOR_ONE,
		VK_BLEND_FACTOR_ONE,
		VK_BLEND_OP_ADD,
		VK_BLEND_FACTOR_ONE,
		VK_BLEND_FACTOR_ONE,
		VK_BLEND_OP_ADD
	);
	public static final BlendMode ALPHA_ADDITIVE = new BlendMode(
		VK_BLEND_FACTOR_SRC_ALPHA,
		VK_BLEND_FACTOR_ONE,
		VK_BLEND_OP_ADD,
		VK_BLEND_FACTOR_SRC_ALPHA,
		VK_BLEND_FACTOR_ONE,
		VK_BLEND_OP_ADD
	);

	private int srcColorBlendFactor;
	private int dstColorBlendFactor;
	private int colorBlendOp;
	private int srcAlphaBlendFactor;
	private int dstAlphaBlendFactor;
	private int alphaBlendOp;

}
