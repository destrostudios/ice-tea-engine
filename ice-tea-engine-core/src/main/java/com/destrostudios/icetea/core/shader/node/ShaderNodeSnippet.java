package com.destrostudios.icetea.core.shader.node;

import com.destrostudios.icetea.core.shader.ShaderHookPosition;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashSet;

@AllArgsConstructor
@Getter
public class ShaderNodeSnippet {
    private String hookedHookName;
    private ShaderHookPosition hookedHookPosition;
    private String text;
    private HashSet<String> ownHooks;
}
