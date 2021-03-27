package com.destrostudios.icetea.core.collision;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BIHLeaf extends BIHTreeItem {
    private int leftTriangleIndex;
    private int rightTriangleIndex;
}
