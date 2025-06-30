package com.destrostudios.icetea.core.shader.node;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashSet;

@AllArgsConstructor
@Getter
public class ShaderNode {
    private HashSet<ShaderNodeSnippet> snippets;
}
