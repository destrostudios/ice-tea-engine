package com.destrostudios.icetea.core.shader;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ShaderHookPosition {
    BEFORE("before"),
    SELF(""),
    AFTER("after");

    private String extension;
}
