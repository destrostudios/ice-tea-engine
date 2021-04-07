package com.destrostudios.icetea.core.material.descriptor;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MaterialDescriptorWithLayout {
    private MaterialDescriptorLayout layout;
    private MaterialDescriptor descriptor;
}
